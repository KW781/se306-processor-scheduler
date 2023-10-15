package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.enums.Heuristic;
import javafx.util.Pair;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Partial/Complete Task Schedule
 */
public class ScheduleNode {
    // Total schedule nodes created, used to identify each ScheduleNode
    private static int numCreated = 0;

    //Task Id as key, data is pair of processor run on and end time.
    private Map<String, Pair<Integer, Integer>> visited;
    private Set<Node> availableTasks;
    private List<Integer> processorEndTimes;
    private List<Node> processorLastTasks;
    // If not null, represents the fixed task order of the ScheduleNode
    private List<Node> fixedTaskOrder;
    // All the tasks scheduled on each processor
    private List<List<Node>> processorTasks;
    // The last task scheduled
    private Node lastTask;
    // The last processor a task was added to
    private Integer lastProcessor;
    // Number of processors available for scheduling
    private Integer processorCount;
    private ScheduleNode parent;
    // Estimate cost of complete schedule
    private Integer fValue = 0;
    private Integer idleTime = 0;
    private Integer completedTaskDuration = 0;
    // True if at any point, the schedule had a fixed task order
    private boolean hadFixedTaskOrder = false;
    // The heuristic used to generate the fValue
    private Heuristic heuristicUsed;
    // True if any unpromising children were detected on expansion
    private boolean unpromisingChildren = false;
    private int id;
    private int threadId = 0;

    /**
     * Creates the initial empty schedule, ready for expansion.
     *
     * @param processorCount The number of processors available
     * @param startingTasks List of the all the initially available to schedule tasks
     */
    public ScheduleNode(Integer processorCount, Set<Node> startingTasks) {
        this.setAvailableTasks(startingTasks);
        this.setProcessorCount(processorCount);
        setVisited(new HashMap<>());
        setProcessorEndTimes(new ArrayList<>());
        setProcessorLastTasks(new ArrayList<>());
        setProcessorTasks(new ArrayList<>());

        for (int i = 0; i < processorCount; i++) {
            getProcessorEndTimes().add(0);
            getProcessorLastTasks().add(null);
            getProcessorTasks().add(new ArrayList<>());
        }

        setId(getNumCreated());
        setNumCreated(getNumCreated() + 1);
    }

    /**
     * Creates an expansion of the provided ScheduleNode by creating a deep copy of the ScheduleNode
     * and then schedules according to the specified task and processor.
     * Also stores an updated copy of the fixed task order if applicable.
     *
     * @param copy The ScheduleNode to expand
     * @param newTask The new task to schedule
     * @param processor The processor to schedule the new task on
     * @param fixedTaskOrder The fixed task order to expand this ScheduleNode. Can be null if none exists.
     */
    public ScheduleNode(ScheduleNode copy, Node newTask, Integer processor, List<Node> fixedTaskOrder) {
        this.setFixedTaskOrder(fixedTaskOrder);
        this.setHadFixedTaskOrder(fixedTaskOrder != null || copy.isHadFixedTaskOrder());
        this.setVisited(new HashMap<>(copy.getVisited()));
        this.setAvailableTasks(new HashSet<>(copy.getAvailableTasks()));
        this.setProcessorEndTimes(new ArrayList<>(copy.getProcessorEndTimes()));
        this.setProcessorLastTasks(new ArrayList<>(copy.getProcessorLastTasks()));
        this.setProcessorCount(copy.getProcessorCount());
        this.setParent(copy);
        this.setIdleTime(copy.getIdleTime());
        this.setfValue(0);

        this.setProcessorTasks(new ArrayList<>());

        for (List<Node> taskIds : copy.getProcessorTasks()) {
            getProcessorTasks().add(new ArrayList<>(taskIds));
        }

        this.setCompletedTaskDuration(copy.getCompletedTaskDuration());

        addTask(newTask, processor);

        setId(getNumCreated());
        setNumCreated(getNumCreated() + 1);
    }

    public static int getNumCreated() {
        return numCreated;
    }

    public static void setNumCreated(int numCreated) {
        ScheduleNode.numCreated = numCreated;
    }

    /**
     * @return Tasks visited by this ScheduleNode
     */
    public Map<String, Pair<Integer, Integer>> getVisited() {
        return visited;
    }

    /**
     * Sort the provided tasks in a fixed order for expansion by modifying the provided list.
     *
     * @param tasks The tasks to sort.
     * @return true if the tasks were successfully sorted, false otherwise.
     */
    private boolean sortFixedOrderTasks(List<Node> tasks) {
        tasks.sort((a, b) -> {
            // Sort tasks by their non-decreasing data ready time
            // DRT = Finish time of parent + weight of edge (parent -> task)
            // With no parent, DRT = 0
            int aDRT = SchedulingProblem.calculateMaxDRT(a, -1, getVisited());
            int bDRT = SchedulingProblem.calculateMaxDRT(b, -1, getVisited());

            if (aDRT > bDRT) {
                return 1;
            }

            if (aDRT < bDRT) {
                return -1;
            }

            // If DRT is equal, sort according to non-increasing out-edge costs.
            // With no child, out-edge cost == 0
            int aOutEdgeCost = 0;
            int bOutEdgeCost = 0;
            try {
                aOutEdgeCost = a.leavingEdges().findFirst().orElseThrow().getAttribute("Weight", Double.class).intValue();

            } catch (NoSuchElementException ignored) {}

            try {
                bOutEdgeCost = b.leavingEdges().findFirst().orElseThrow().getAttribute("Weight", Double.class).intValue();

            } catch (NoSuchElementException ignored) {}

            return Integer.compare(bOutEdgeCost, aOutEdgeCost);
        });

        // If the final order is not in non-increasing out edge cost order,
        // then it is not a valid fixed order.
        int prevOutEdgeCost = Integer.MAX_VALUE;
        for (Node task : tasks) {
            int outEdgeCost = 0;

            try {
                outEdgeCost = task.leavingEdges().findFirst().orElseThrow().getAttribute("Weight", Double.class).intValue();

            } catch (NoSuchElementException ignored) {}

            if (outEdgeCost > prevOutEdgeCost) {
                return false;
            }

            prevOutEdgeCost = outEdgeCost;
        }

        return true;
    }

    /**
     * Verifies if the provided tasks meet the conditions for a fixed task order.
     * Then generates a fixed order of the provided tasks if possible.
     *
     * @param tasks The tasks to generate a fixed order with.
     * @return The fixed order. null if not possible.
     */
    private List<Node> getFixedTaskOrder(List<Node> tasks) {
        String childId = null;
        Integer parentProcessor = null;

        for (Node task : tasks) {
            // Each task must have at most one parent and one child
            int inDegree = (int) task.enteringEdges().filter(edge -> !edge.getId().contains("virtual")).count();
            int outDegree = (int) task.leavingEdges().filter(edge -> !edge.getId().contains("virtual")).count();
            if (inDegree > 1 || outDegree > 1) {
                return null;
            }

            try {
                // If a task has a child, all other tasks with a child must also have the same child
                String child = task.leavingEdges().findFirst().orElseThrow().getTargetNode().getId();
                if (childId == null) {
                    childId = child;
                } else if (!childId.equals(child)) {
                    return null;
                }
            } catch (NoSuchElementException ignored) {}

            try {
                // If a task has a parent, all other tasks' parent must be allocated to the same processor
                String parent = task.enteringEdges().findFirst().orElseThrow().getSourceNode().getId();
                if (parentProcessor == null) {
                    parentProcessor = getVisited().get(parent).getKey();
                } else if (!parentProcessor.equals(getVisited().get(parent).getKey())) {
                    return null;
                }
            } catch (NoSuchElementException ignored) {}
        }

        if (!sortFixedOrderTasks(tasks)) {
            return null;
        }

        return tasks;
    }

    /**
     * Expands the current ScheduleNode using Fixed Task Ordering.
     * Utilises Partial Expansion, thus the current ScheduleNode
     * must be re-added to the search if ScheduleNode.unpromisingChildren is set to true.
     *
     * @return A list the expanded ScheduleNodes.
     */
    private List<ScheduleNode> generateNeighboursWithFTO() {
        List<ScheduleNode> neighbours = new ArrayList<>();
        int minUnpromising = Integer.MAX_VALUE;

        // If fixed task order is empty, there are no expansions possible.
        if (getFixedTaskOrder().isEmpty()) {
            return new ArrayList<>();
        }

        // Expands by scheduling the first task in the fixed order on every available processor.
        Node task = getFixedTaskOrder().get(0);
        List<Node> newFixedTaskOrder = getFixedTaskOrder().subList(1, getFixedTaskOrder().size());

        for (int p = 0; p < getProcessorCount(); p++) {
            ScheduleNode childSchedule = new ScheduleNode(this, task, p, newFixedTaskOrder);
            SchedulingProblem.calculateF(childSchedule);

            // Checks if fixed task order is still possible without recalculations.
            // If the Child Schedule has added new tasks to availableTasks, then a recalculation is required.
            if (childSchedule.getAvailableTasks().size() > getAvailableTasks().size() - 1) {
                childSchedule.setFixedTaskOrder(null);
            }

            // Performs Partial Expansion.
            // Adds the Child Schedule if it's F value is <= the current F value
            // Otherwise, stores the minimum F value of the unpromising children.
            if (childSchedule.getfValue() <= this.getfValue()) {
                neighbours.add(childSchedule);
            } else {
                minUnpromising = Math.min(minUnpromising, childSchedule.getfValue());
            }
        }

        // If != Integer.MAX_VALUE, it means unpromising children were found.
        // Updates the ScheduleNode's F value accordingly and sets unpromisingChildren flag to true.
        if (minUnpromising != Integer.MAX_VALUE) {
            setUnpromisingChildren(true);
            this.setfValue(minUnpromising);
        }

        return neighbours;
    }

    /**
     * Expands the current ScheduleNode using Fixed Task Ordering.
     * Utilises Partial Expansion, thus the current ScheduleNode
     * must be re-added to the search if ScheduleNode.unpromisingChildren is set to true.
     *
     * @return A list the expanded ScheduleNodes.
     */
    public List<ScheduleNode> generateNeighbours() {
        List<ScheduleNode> neighbours = new ArrayList<>();
        int minUnpromising = Integer.MAX_VALUE;

        // Checks and expands via Fixed Task Ordering if possible.
        if (getFixedTaskOrder() == null) {
            setFixedTaskOrder(getFixedTaskOrder(new ArrayList<>(getAvailableTasks())));
        }

        if (getFixedTaskOrder() != null) {
            return generateNeighboursWithFTO();
        }

        // Expands normally using Partial Expansion.
        for (Node task : getAvailableTasks()) {
            for (int i = 0; i < getProcessorCount(); i++) {
                ScheduleNode childSchedule = new ScheduleNode(this, task, i, null);
                SchedulingProblem.calculateF(childSchedule);

                // Adds the Child Schedule if it's F value is <= the current F value
                // Otherwise, stores the minimum F value of the unpromising children.
                if (childSchedule.getfValue() <= this.getfValue()) {
                    neighbours.add(childSchedule);
                } else {
                    minUnpromising = Math.min(minUnpromising, childSchedule.getfValue());
                }
            }
        }

        // If != Integer.MAX_VALUE, it means unpromising children were found.
        // Updates the ScheduleNode's F value accordingly and sets unpromisingChildren flag to true.
        if (minUnpromising != Integer.MAX_VALUE) {
            setUnpromisingChildren(true);
            this.setfValue(minUnpromising);
        }

        return neighbours;
    }

    /**
     * @return Number of tasks scheduled
     */
    public int getNumTasksScheduled() {
        return getVisited().size();
    }

    /**
     * Checks if the ScheduleNode is a complete schedule
     * @param taskCount The total number of tasks we must visit to complete.
     * @return
     */
    public boolean isComplete(Integer taskCount) {
        return (taskCount == getVisited().size());
    }

    /**
     *
     * @return The F value of this ScheduleNode
     */
    public Integer getfValue() {
        return this.fValue;
    }

    /**
     * @return The latest processor end time of this ScheduleNode
     */
    public Integer getValue() {
        int result = 0;

        for (int i = 0; i < getProcessorEndTimes().size(); i++) {
            result = Math.max(result, getProcessorEndTimes().get(i));
        }

        return result;
    }

    /**
     * @return The highest cost path of this ScheduleNode
     */
    public Integer getPathCost() {
        return getValue();
    }

    /**
     * @param processor The processor you want the path cost of.
     * @return The path cost of the specified processor.
     */
    public Integer getProcessorPathCost(Integer processor) {
        return getProcessorEndTimes().get(processor);
    }

    /**
     * Schedules the specified task onto the specified processor.
     * @param newTask The task to schedule.
     * @param processor The processor to schedule on.
     */
    private void addTask(Node newTask, Integer processor) {
        List<Edge> incomingEdges = newTask.enteringEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());

        Integer earliestStartTime = getProcessorEndTimes().get(processor);
        Integer previousEndTime = earliestStartTime;

        // Getting the earliest possible start time for this task when scheduled on the specified processor
        for (Edge incomingEdge : incomingEdges) {
            Integer parentEndTime = getVisited().get(incomingEdge.getSourceNode().getId()).getValue();

            if (getVisited().get(incomingEdge.getSourceNode().getId()).getKey() != processor) {
                parentEndTime += incomingEdge.getAttribute("Weight", Double.class).intValue();
            }

            earliestStartTime = Math.max(earliestStartTime, parentEndTime);
        }
        // Calculating the end time of the newly scheduled task.
        Integer endTime = earliestStartTime + newTask.getAttribute("Weight", Double.class).intValue();

        // Updates ScheduleNode information according to the task just scheduled.
        getVisited().put(newTask.getId(), new Pair<>(processor, endTime));
        getProcessorEndTimes().set(processor, endTime);
        getProcessorTasks().get(processor).add(newTask);
        getProcessorLastTasks().set(processor, newTask);
        getAvailableTasks().remove(newTask);
        setLastTask(newTask);
        setLastProcessor(processor);
        setIdleTime(getIdleTime() + earliestStartTime - previousEndTime);

        setCompletedTaskDuration(getCompletedTaskDuration() + newTask.getAttribute("Weight", Double.class).intValue());

        addNewTasks(newTask);
    }

    /**
     * Calculates an estimate of the minimum parent data arrival time of a task recursively.
     * This information is used for Equivalent Schedule pruning.
     *
     * @param node The parent to calculate for.
     * @param time The current data arrival time of the child task.
     * @param newProcessorEndTime The new end time of the processor modified during Equivalent Schedule pruning.
     * @return Estimate of the minimum parent data arrival time.
     */
    private int estimateParentDataArrivalTime(Node node, int time, int newProcessorEndTime) {
        int minTime = Integer.MAX_VALUE;

        // If task has been visited, then return its end time + current DRT of it's child
        if (getVisited().containsKey(node.getId())) {
            return time + getVisited().get(node.getId()).getValue();
        }

        // Gets the minimum DRT amongst the task's parents
        for (Node parent : node.enteringEdges().map(Edge::getSourceNode).collect(Collectors.toList())) {
            minTime = Math.min(estimateParentDataArrivalTime(parent, node.getAttribute("Weight", Double.class).intValue() + time, newProcessorEndTime), minTime);
        }

        // If still equal to Integer.MAX_VALUE, it means current task is not scheduled and has no parent.
        // So we estimate min DRT by returning the earliest time the task can be scheduled.
        if (minTime == Integer.MAX_VALUE) {
            for (int i = 0; i < getProcessorCount(); i++) {
                if (i == getLastProcessor()) {
                    minTime = Math.min(newProcessorEndTime, minTime);;
                }

                minTime = Math.min(getProcessorEndTimes().get(i), minTime);
            }
            minTime = minTime + node.getAttribute("Weight", Double.class).intValue();
        }

        return minTime;
    }

    /**
     * Checks if the change to the task ordering does not delay the start of
     * any child task of the tasks in the list supplied when compared to the original schedule.
     * @param tasks List of the changed task ordering along with their new end times
     * @param newProcessorEndTime The processor end time associated with the changed task ordering
     * @return True if no delay, otherwise false
     */
    private boolean outgoingCommsOK(List<Pair<Node, Integer>> tasks, int newProcessorEndTime) {
        for (Pair<Node, Integer> taskEndTime : tasks) {
            Node task = taskEndTime.getKey();
            int endTime = taskEndTime.getValue();
            // It can only delay if the new end time is greater than the previous end time
            if (endTime > getVisited().get(task.getId()).getValue()) {
                // Checks each child of the task for whether it's start time is delayed
                for (Edge childEdge : task.leavingEdges().collect(Collectors.toList())) {
                    int newDataArrivalTime = endTime + childEdge.getAttribute("Weight", Double.class).intValue();
                    Node child = childEdge.getTargetNode();
                    // If child is already scheduled, checks if the original start time is greater than the new DRT
                    // If the child is on the same processor as the task, then there shouldn't be any issues.
                    if (getVisited().containsKey(child.getId())) {
                        Pair<Integer, Integer> childProcessorEndTime = getVisited().get(child.getId());
                        int originalStartTime = childProcessorEndTime.getValue() - child.getAttribute("Weight", Double.class).intValue();
                        if (originalStartTime > newDataArrivalTime && childProcessorEndTime.getKey() != getLastProcessor()) {
                            return false;
                        }
                    } else {
                        // If child is not scheduled, then child can be scheduled on any processor.
                        // For each processor, we hypothetically schedule the child task.
                        // If the DRT of the child task on any processor is greater than the new DRT,
                        // then there are no delays.
                        for (int p = 0; p < getProcessorCount(); p++) {
                            if (p == getLastProcessor()) {
                                continue;
                            }

                            // Checking if there is a DRT from at least one parent to this processor that is
                            // greater than the new DRT.
                            for (Edge parentEdge : child.enteringEdges().collect(Collectors.toList())) {
                                Node parent = parentEdge.getSourceNode();

                                if (parent.getIndex() == task.getIndex()) {
                                    continue;
                                }

                                Pair<Integer, Integer> parentProcessorEndTime = getVisited().get(parent.getId());

                                int parentDataArrivalTime;
                                if (parentProcessorEndTime == null) {
                                    // If parent is not scheduled, we estimate the best case DRT.
                                    parentDataArrivalTime = estimateParentDataArrivalTime(parent, 0, newProcessorEndTime);
                                } else {
                                    // Otherwise, DRT is just parent end time + edge weight between parent and child.
                                    parentDataArrivalTime = parentProcessorEndTime.getValue() + parentEdge.getAttribute("Weight", Double.class).intValue();
                                }

                                if (parentDataArrivalTime >= newDataArrivalTime) {
                                    return true;
                                }
                            }

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if an equivalent schedule is possible by sorting the tasks of the
     * last scheduled processor in ascending index order.
     *
     * @return true if possible, otherwise false.
     */
    public boolean isEquivalent() {
        // If no task has been scheduled, then return false.
        if (getLastTask() == null || getLastProcessor() == null) {
            return false;
        }

        // Makes a deep copy of the tasks scheduled on the last processor.
        List<Node> tasks = new ArrayList<>(getProcessorTasks().get(getLastProcessor()));
        // Gets all the parents of the last scheduled task.
        Set<Node> lastTaskParents = getLastTask().enteringEdges().map(Edge::getTargetNode).collect(Collectors.toSet());

        int maxTimeToFinish = getProcessorEndTimes().get(getLastProcessor());
        int i = tasks.size() - 2;
        int lastTaskId = getLastTask().getIndex();

        while (i >= 0 && lastTaskId < tasks.get(i).getIndex()) {
            List<Pair<Node, Integer>> taskEndTimes = new ArrayList<>();
            // If the task is the last task's parent, can not swap due without violating the prerequisite.
            // Immediately return false.
            if (lastTaskParents.contains(tasks.get(i))) {
                return false;
            }
            Collections.swap(tasks, i + 1, i);

            int endTime = 0;
            // For each task we must recalculate its end time.
            for (Node task : tasks) {
                Iterable<Edge> parents = task.enteringEdges().collect(Collectors.toList());

                int earliestStartTime = endTime;

                // Getting the earliest start time of task
                for (Edge parent : parents) {
                    Integer parentEndTime = getVisited().get(parent.getSourceNode().getId()).getValue();

                    if (getVisited().get(parent.getSourceNode().getId()).getKey() != getLastProcessor()) {
                        parentEndTime += parent.getAttribute("Weight", Double.class).intValue();
                    }

                    earliestStartTime = Math.max(earliestStartTime, parentEndTime);
                }

                // Calculating the earliest end time.
                // At the end of the loop, end time should equal the end time of the last task on the processor
                endTime = earliestStartTime + task.getAttribute("Weight", Double.class).intValue();
                taskEndTimes.add(new Pair<>(task, endTime));
            }

            // New end time must be <= the original end time.
            // New task order must not delay the start of any child task in the current list of tasks
            if (endTime <= maxTimeToFinish && outgoingCommsOK(taskEndTimes, endTime)) {
                return true;
            }

            i--;
        }

        return false;
    }

    /**
     * Checking if any news tasks have met their prerequisites after the specified task has been scheduled.
     *
     * @param newTask The newly scheduled task.
     */
    private void addNewTasks(Node newTask) {
        Iterable<Edge> children = newTask.leavingEdges().collect(Collectors.toList());

        for (Edge child : children) {
            boolean prereqsMet = true;
            Iterable<Edge> dependencies = child.getTargetNode().enteringEdges().collect(Collectors.toList());

            // If any dependency has not been scheduled, then it does not meet the prerequisites.
            for (Edge dependency : dependencies) {
                if (!getVisited().containsKey(dependency.getSourceNode().getId())) {
                    prereqsMet = false;
                    break;
                }
            }

            if (prereqsMet) {
                getAvailableTasks().add(child.getTargetNode());
            }
        }
    }

    /**
     * @return The parent ScheduleNode
     */
    public ScheduleNode getParent() {
        return parent;
    }

    /**
     * Checks if the ScheduleNode is equal to the provided object.
     * Uses Processor Normalisation to determine equality.
     *
     * @param o The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleNode other = (ScheduleNode) o;

        if (getVisited().size() != other.getVisited().size()) {
            return false;
        }

        // For each visited task, both processors must have visited the task
        // and both processors must have the same task end time for the respective task.
        for (String nodeId : getVisited().keySet()) {
            Pair<Integer, Integer> otherProcessorEndTime = other.getVisited().get(nodeId);

            if (otherProcessorEndTime == null) {
                return false;
            }

            Pair<Integer, Integer> thisProcessorEndTime = getVisited().get(nodeId);

            if (!thisProcessorEndTime.getValue().equals(otherProcessorEndTime.getValue())) {
                return false;
            }
        }

        return true;
    }

    public Heuristic getHeuristicUsed() {
        return this.heuristicUsed;
    }


    /**
     * Hashes the ScheduleNode based on its tasks visited and respective end times.
     * @return hash value
     */
    @Override
    public int hashCode() {
        Map<String, Integer> nodeEndTime = new HashMap<>();

        for (String nodeId : getVisited().keySet()) {
            Pair<Integer, Integer> processorEndTime = getVisited().get(nodeId);

            nodeEndTime.put(nodeId, processorEndTime.getValue());
        }

        return Objects.hash(nodeEndTime);
    }

    /**
     * Uses a unique identifier to represent the ScheduleNode in String form.
     * @return the String representation of the ScheduleNode.
     */
    @Override
    public String toString() {
        return "ScheduleNode{" +
                "id=" + getId() +
                '}';
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getThreadId() {
        return this.threadId;
    }

    public void setVisited(Map<String, Pair<Integer, Integer>> visited) {
        this.visited = visited;
    }

    public Set<Node> getAvailableTasks() {
        return availableTasks;
    }

    public void setAvailableTasks(Set<Node> availableTasks) {
        this.availableTasks = availableTasks;
    }

    public List<Integer> getProcessorEndTimes() {
        return processorEndTimes;
    }

    public void setProcessorEndTimes(List<Integer> processorEndTimes) {
        this.processorEndTimes = processorEndTimes;
    }

    public List<Node> getProcessorLastTasks() {
        return processorLastTasks;
    }

    public void setProcessorLastTasks(List<Node> processorLastTasks) {
        this.processorLastTasks = processorLastTasks;
    }

    public List<Node> getFixedTaskOrder() {
        return fixedTaskOrder;
    }

    public void setFixedTaskOrder(List<Node> fixedTaskOrder) {
        this.fixedTaskOrder = fixedTaskOrder;
    }

    public List<List<Node>> getProcessorTasks() {
        return processorTasks;
    }

    public void setProcessorTasks(List<List<Node>> processorTasks) {
        this.processorTasks = processorTasks;
    }

    public Node getLastTask() {
        return lastTask;
    }

    public void setLastTask(Node lastTask) {
        this.lastTask = lastTask;
    }

    public Integer getLastProcessor() {
        return lastProcessor;
    }

    public void setLastProcessor(Integer lastProcessor) {
        this.lastProcessor = lastProcessor;
    }

    public Integer getProcessorCount() {
        return processorCount;
    }

    public void setProcessorCount(Integer processorCount) {
        this.processorCount = processorCount;
    }

    public void setParent(ScheduleNode parent) {
        this.parent = parent;
    }

    public void setfValue(Integer fValue) {
        this.fValue = fValue;
    }

    public Integer getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(Integer idleTime) {
        this.idleTime = idleTime;
    }

    public Integer getCompletedTaskDuration() {
        return completedTaskDuration;
    }

    public void setCompletedTaskDuration(Integer completedTaskDuration) {
        this.completedTaskDuration = completedTaskDuration;
    }

    public boolean isHadFixedTaskOrder() {
        return hadFixedTaskOrder;
    }

    public void setHadFixedTaskOrder(boolean hadFixedTaskOrder) {
        this.hadFixedTaskOrder = hadFixedTaskOrder;
    }

    public void setHeuristicUsed(Heuristic heuristicUsed) {
        this.heuristicUsed = heuristicUsed;
    }

    public boolean isUnpromisingChildren() {
        return unpromisingChildren;
    }

    public void setUnpromisingChildren(boolean unpromisingChildren) {
        this.unpromisingChildren = unpromisingChildren;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
