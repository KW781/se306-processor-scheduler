package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.enums.Heuristic;
import javafx.util.Pair;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduleNode {
    static int numCreated = 0;

    //Task Id as key, data is pair of processor run on and end time.
    Map<String, Pair<Integer, Integer>> visited;
    Set<Node> availableTasks;
    List<Integer> processorEndTimes;
    List<Node> processorLastTasks;
    List<Node> fixedTaskOrder;
    List<List<Node>> processorTasks;
    Node lastTask;
    Integer lastProcessor;
    Integer processorCount;
    ScheduleNode parent;
    Integer fValue = 0;
    Integer idleTime = 0;
    public Integer completedTaskDuration = 0;
    boolean hadFixedTaskOrder = false;
    Heuristic heuristicUsed;
    boolean unpromisingChildren = false;
    int id;

    public ScheduleNode(Integer processorCount, Set<Node> startingTasks) {
        this.availableTasks = startingTasks;
        this.processorCount = processorCount;
        visited = new HashMap<>();
        processorEndTimes = new ArrayList<>();
        processorLastTasks = new ArrayList<>();
        processorTasks = new ArrayList<>();

        for (int i = 0; i < processorCount; i++) {
            processorEndTimes.add(0);
            processorLastTasks.add(null);
            processorTasks.add(new ArrayList<>());
        }

        id = numCreated;
        numCreated++;
    }

    public ScheduleNode(ScheduleNode copy, Node newTask, Integer processor, List<Node> fixedTaskOrder) {
        this.fixedTaskOrder = fixedTaskOrder;
        this.hadFixedTaskOrder = fixedTaskOrder != null || copy.hadFixedTaskOrder;
        this.visited = new HashMap<>(copy.visited);
        this.availableTasks = new HashSet<>(copy.availableTasks);
        this.processorEndTimes = new ArrayList<>(copy.processorEndTimes);
        this.processorLastTasks = new ArrayList<>(copy.processorLastTasks);
        this.processorCount = copy.processorCount;
        this.parent = copy;
        this.idleTime = copy.idleTime;
        this.fValue = 0;

        this.processorTasks = new ArrayList<>();

        for (List<Node> taskIds : copy.processorTasks) {
            processorTasks.add(new ArrayList<>(taskIds));
        }

        this.completedTaskDuration = copy.completedTaskDuration;

        addTask(newTask, processor);

        id = numCreated;
        numCreated++;
    }

    public Map<String, Pair<Integer, Integer>> GetVisited() {
        return visited;
    }

    private boolean SortFixedOrderTasks(List<Node> tasks) {
        // Task sorting conditions
        // Sort tasks by their non-decreasing data ready time
        // DRT = Finish time of parent + weight of edge (parent -> task)
        // With no parent, DRT = 0
        // If DRT is equal, sort according to non-increasing out-edge costs.
        // With no child, out-edge cost == 0
        tasks.sort((a, b) -> {
            int aDRT = 0;
            int bDRT = 0;
            try {
                Edge incomingEdge = a.enteringEdges().findFirst().orElseThrow();
                aDRT = visited.get(incomingEdge.getSourceNode().getId()).getValue() + incomingEdge.getAttribute("Weight", Double.class).intValue();

            } catch (NoSuchElementException ignored) {}

            try {
                Edge incomingEdge = b.enteringEdges().findFirst().orElseThrow();
                bDRT = visited.get(incomingEdge.getSourceNode().getId()).getValue() + incomingEdge.getAttribute("Weight", Double.class).intValue();

            } catch (NoSuchElementException ignored) {}

            if (aDRT > bDRT) {
                return 1;
            }

            if (aDRT < bDRT) {
                return -1;
            }

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

    private List<Node> GetFixedTaskOrder(List<Node> tasks) {
        // Verifying Fixing order conditions
        // 1. Each task must have at most one parent and one child
        // 2. If a task has a child, all other tasks with a child must also have the same child
        // 3. If a task has a parent, all other tasks' parent must be allocated to the same processor
        String childId = null;
        Integer parentProcessor = null;

        for (Node task : tasks) {
            if (task.getInDegree() > 1 || task.getOutDegree() > 1) {
                return null;
            }

            try {
                String child = task.leavingEdges().findFirst().orElseThrow().getTargetNode().getId();
                if (childId == null) {
                    childId = child;
                } else if (!childId.equals(child)) {
                    return null;
                }
            } catch (NoSuchElementException ignored) {}

            try {
                String parent = task.enteringEdges().findFirst().orElseThrow().getSourceNode().getId();
                if (parentProcessor == null) {
                    parentProcessor = visited.get(parent).getKey();
                } else if (!parentProcessor.equals(visited.get(parent).getKey())) {
                    return null;
                }
            } catch (NoSuchElementException ignored) {}
        }

        if (!SortFixedOrderTasks(tasks)) {
            return null;
        }

        return tasks;
    }

    private List<ScheduleNode> GenerateNeighboursWithFTO() {
        List<ScheduleNode> neighbours = new ArrayList<>();
        int minUnpromising = Integer.MAX_VALUE;

        if (fixedTaskOrder.isEmpty()) {
            return new ArrayList<>();
        }

        Node task = fixedTaskOrder.get(0);
        List<Node> newFixedTaskOrder = fixedTaskOrder.subList(1, fixedTaskOrder.size());

        for (int p = 0; p < processorCount; p++) {
            ScheduleNode childSchedule = new ScheduleNode(this, task, p, newFixedTaskOrder);
            SchedulingProblem.CalculateF(childSchedule);

            if (childSchedule.availableTasks.size() > availableTasks.size() - 1) {
                childSchedule.fixedTaskOrder = null;
            }

            if (childSchedule.fValue <= this.fValue) {
                neighbours.add(childSchedule);
            } else {
                minUnpromising = Math.min(minUnpromising, childSchedule.fValue);
            }
        }

        if (minUnpromising != Integer.MAX_VALUE) {
            unpromisingChildren = true;
            this.fValue = minUnpromising;
        }

        return neighbours;
    }

    public List<ScheduleNode> GenerateNeighbours() {
        List<ScheduleNode> neighbours = new ArrayList<>();
        int minUnpromising = Integer.MAX_VALUE;

        if (fixedTaskOrder == null) {
            fixedTaskOrder = GetFixedTaskOrder(new ArrayList<>(availableTasks));
        }

        if (fixedTaskOrder != null) {
            return GenerateNeighboursWithFTO();
        }

        for (Node task : availableTasks) {
            for (int i = 0; i < processorCount; i++) {
                ScheduleNode childSchedule = new ScheduleNode(this, task, i, null);
                SchedulingProblem.CalculateF(childSchedule);

                if (childSchedule.fValue <= this.fValue) {
                    neighbours.add(childSchedule);
                } else {
                    minUnpromising = Math.min(minUnpromising, childSchedule.fValue);
                }
            }
        }

        if (minUnpromising != Integer.MAX_VALUE) {
            unpromisingChildren = true;
            this.fValue = minUnpromising;
        }

        return neighbours;
    }

    public int getNumTasksScheduled() {
        return visited.size();
    }

    public boolean IsComplete(Integer taskCount) {
        return (taskCount == visited.size());
    }

    public Integer GetValue() {
        int result = 0;

        for (int i = 0; i < processorEndTimes.size(); i++) {
            result = Math.max(result, processorEndTimes.get(i));
        }

        return result;
    }

    public Integer GetPathCost() {
        return GetValue();
    }

    public Integer GetProcessorPathCost(Integer processor) {
        return processorEndTimes.get(processor);
    }

    private void addTask(Node newTask, Integer processor) {
        List<Edge> incomingEdges = newTask.enteringEdges().collect(Collectors.toList());

        Integer earliestStartTime = processorEndTimes.get(processor);
        Integer previousEndTime = earliestStartTime;

        for (Edge incomingEdge : incomingEdges) {
            Integer parentEndTime = visited.get(incomingEdge.getSourceNode().getId()).getValue();

            if (visited.get(incomingEdge.getSourceNode().getId()).getKey() != processor) {
                parentEndTime += incomingEdge.getAttribute("Weight", Double.class).intValue();
            }

            earliestStartTime = Math.max(earliestStartTime, parentEndTime);
        }

        Integer endTime = earliestStartTime + newTask.getAttribute("Weight", Double.class).intValue();

        visited.put(newTask.getId(), new Pair<>(processor, endTime));
        processorEndTimes.set(processor, endTime);
        processorTasks.get(processor).add(newTask);
        processorLastTasks.set(processor, newTask);
        availableTasks.remove(newTask);
        lastTask = newTask;
        lastProcessor = processor;
        idleTime += earliestStartTime - previousEndTime;

        completedTaskDuration += newTask.getAttribute("Weight", Double.class).intValue();

        AddNewTasks(newTask);
    }

    private int estimateParentDataArrivalTime(Node node, int time, int newProcessorEndTime) {
        int minTime = Integer.MAX_VALUE;

        if (visited.containsKey(node.getId())) {
            return time + visited.get(node.getId()).getValue();
        }

        for (Node parent : node.enteringEdges().map(Edge::getSourceNode).collect(Collectors.toList())) {
            minTime = Math.min(estimateParentDataArrivalTime(parent, node.getAttribute("Weight", Double.class).intValue() + time, newProcessorEndTime), minTime);
        }

        // If still equal, it means current node is not scheduled and has no parent
        if (minTime == Integer.MAX_VALUE) {
            for (int i = 0; i < processorCount; i++) {
                if (i == lastProcessor) {
                    minTime = Math.min(newProcessorEndTime, minTime);;
                }

                minTime = Math.min(processorEndTimes.get(i), minTime);
            }
            minTime = minTime + node.getAttribute("Weight", Double.class).intValue();
        }

        return minTime;
    }

    private boolean OutgoingCommsOK(List<Pair<Node, Integer>> tasks, int newProcessorEndTime) {
        for (Pair<Node, Integer> taskEndTime : tasks) {
            Node task = taskEndTime.getKey();
            int endTime = taskEndTime.getValue();
            if (endTime > visited.get(task.getId()).getValue()) {
                for (Edge childEdge : task.leavingEdges().collect(Collectors.toList())) {
                    int newDataArrivalTime = endTime + childEdge.getAttribute("Weight", Double.class).intValue();
                    Node child = childEdge.getTargetNode();
                    if (visited.containsKey(child.getId())) {
                        Pair<Integer, Integer> childProcessorEndTime = visited.get(child.getId());
                        int originalStartTime = childProcessorEndTime.getValue() - child.getAttribute("Weight", Double.class).intValue();
                        if (originalStartTime > newDataArrivalTime && childProcessorEndTime.getKey() != lastProcessor) {
                            return false;
                        }
                    } else {
                        for (int p = 0; p < processorCount; p++) {
                            if (p == lastProcessor) {
                                continue;
                            }

                            for (Edge parentEdge : child.enteringEdges().collect(Collectors.toList())) {
                                Node parent = parentEdge.getSourceNode();

                                if (parent.getIndex() == task.getIndex()) {
                                    continue;
                                }

                                Pair<Integer, Integer> parentProcessorEndTime = visited.get(parent.getId());

                                int parentDataArrivalTime;
                                if (parentProcessorEndTime == null) {
                                    // Gets best case data arrival time estimate
                                    parentDataArrivalTime = estimateParentDataArrivalTime(parent, 0, newProcessorEndTime);
                                } else {
                                    parentDataArrivalTime = parentProcessorEndTime.getValue() + parent.getAttribute("Weight", Double.class).intValue();
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

    public boolean IsEquivalent() {
        if (lastTask == null || lastProcessor == null) {
            return false;
        }

        List<Node> tasks = new ArrayList<>(processorTasks.get(lastProcessor));
        Set<Node> lastTaskParents = lastTask.enteringEdges().map(Edge::getTargetNode).collect(Collectors.toSet());

        int maxTimeToFinish = processorEndTimes.get(lastProcessor);
        int i = tasks.size() - 2;
        int lastTaskId = lastTask.getIndex();

        while (i >= 0 && lastTaskId < tasks.get(i).getIndex()) {
            List<Pair<Node, Integer>> taskEndTimes = new ArrayList<>();
            if (lastTaskParents.contains(tasks.get(i))) {
                return false;
            }
            Collections.swap(tasks, i + 1, i);

            int endTime = 0;
            for (Node task : tasks) {
                Iterable<Edge> parents = task.enteringEdges().collect(Collectors.toList());

                int earliestStartTime = endTime;

                for (Edge parent : parents) {
                    Integer parentEndTime = visited.get(parent.getSourceNode().getId()).getValue();

                    if (visited.get(parent.getSourceNode().getId()).getKey() != lastProcessor) {
                        parentEndTime += parent.getAttribute("Weight", Double.class).intValue();
                    }

                    earliestStartTime = Math.max(earliestStartTime, parentEndTime);
                }

                endTime = earliestStartTime + task.getAttribute("Weight", Double.class).intValue();
                taskEndTimes.add(new Pair<>(task, endTime));
            }

            if (endTime <= maxTimeToFinish && OutgoingCommsOK(taskEndTimes, endTime)) {
                return true;
            }

            i--;
        }

        return false;
    }

    private void AddNewTasks(Node newTask) {
        Iterable<Edge> children = newTask.leavingEdges().collect(Collectors.toList());

        for (Edge child : children) {
            boolean prereqsMet = true;
            Iterable<Edge> dependencies = child.getTargetNode().enteringEdges().collect(Collectors.toList());

            for (Edge dependency : dependencies) {
                if (!visited.containsKey(dependency.getSourceNode().getId())) {
                    prereqsMet = false;
                    break;
                }
            }

            if (prereqsMet) {
                availableTasks.add(child.getTargetNode());
            }
        }
    }

    public ScheduleNode GetParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleNode other = (ScheduleNode) o;

        for (String nodeId : visited.keySet()) {
            Pair<Integer, Integer> otherProcessorEndTime = other.visited.get(nodeId);

            if (otherProcessorEndTime == null) {
                return false;
            }

            Pair<Integer, Integer> thisProcessorEndTime = visited.get(nodeId);

            if (!thisProcessorEndTime.getValue().equals(otherProcessorEndTime.getValue())) {
                return false;
            }
        }

        return true;
    }

    public Heuristic getHeuristicUsed() {
        return this.heuristicUsed;
    }



    @Override
    public int hashCode() {
        Map<String, Integer> nodeEndTime = new HashMap<>();

        for (String nodeId : visited.keySet()) {
            Pair<Integer, Integer> processorEndTime = visited.get(nodeId);

            nodeEndTime.put(nodeId, processorEndTime.getValue());
        }

        return Objects.hash(nodeEndTime);
    }

    @Override
    public String toString() {
        return "ScheduleNode{" +
                "id=" + id +
                '}';
    }
}
