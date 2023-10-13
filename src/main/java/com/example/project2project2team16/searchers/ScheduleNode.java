package com.example.project2project2team16.searchers;

import javafx.util.Pair;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduleNode {

    //Task Id as key, data is pair of processor run on and end time.
    Map<String, Pair<Integer, Integer>> visited;
    Set<Node> availableTasks;
    List<Integer> processorEndTimes;
    List<Node> processorLastTasks;
    List<List<Node>> processorTasks;
    Node lastTask;
    Integer lastProcessor;
    Integer processorCount;
    ScheduleNode parent;
    Integer fValue = 0;
    boolean unpromisingChildren = false;

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
    }

    public ScheduleNode(ScheduleNode copy, Node newTask, Integer processor) {
        this.visited = new HashMap<>(copy.visited);
        this.availableTasks = new HashSet<>(copy.availableTasks);
        this.processorEndTimes = new ArrayList<>(copy.processorEndTimes);
        this.processorLastTasks = new ArrayList<>(copy.processorLastTasks);
        this.processorCount = copy.processorCount;
        this.parent = copy;
        this.fValue = 0;

        this.processorTasks = new ArrayList<>();

        for (List<Node> taskIds : copy.processorTasks) {
            processorTasks.add(new ArrayList<>(taskIds));
        }

        AddTask(newTask, processor);
    }

    public Map<String, Pair<Integer, Integer>> GetVisited() {
        return visited;
    }

    public List<ScheduleNode> GenerateNeighbours() {
        List<ScheduleNode> neighbours = new ArrayList<>();
        int minUnpromising = Integer.MAX_VALUE;

        for (Node task : availableTasks) {
            for (int i = 0; i < processorCount; i++) {
                ScheduleNode childSchedule = new ScheduleNode(this, task, i);
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

    private void AddTask(Node newTask, Integer processor) {
        Iterable<Edge> parents = newTask.enteringEdges().collect(Collectors.toList());

        Integer earliestStartTime = processorEndTimes.get(processor);

        for (Edge parent : parents) {
            Integer parentEndTime = visited.get(parent.getSourceNode().getId()).getValue();

            if (visited.get(parent.getSourceNode().getId()).getKey() != processor) {
                parentEndTime += parent.getAttribute("Weight", Double.class).intValue();
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

        AddNewTasks(newTask);
    }

    private boolean OutgoingCommsOK(List<Pair<Node, Integer>> tasks) {
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

                            boolean atLeastOneLater = false;
                            for (Edge parentEdge : child.enteringEdges().collect(Collectors.toList())) {
                                Node parent = parentEdge.getSourceNode();

                                if (Integer.parseInt(parent.getId()) == Integer.parseInt(task.getId())) {
                                    continue;
                                }

                                Pair<Integer, Integer> parentProcessorEndTime = visited.get(parent.getId());

                                if (parentProcessorEndTime == null) {
                                    return false;
                                }

                                int parentDataArrivalTime = parentProcessorEndTime.getValue() + parent.getAttribute("Weight", Double.class).intValue();
                                if (parentDataArrivalTime >= newDataArrivalTime) {
                                    atLeastOneLater = true;
                                }
                            }

                            if (!atLeastOneLater) {
                                return false;
                            }
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
        int lastTaskId = Integer.parseInt(lastTask.getId());

        while (i >= 0 && lastTaskId < Integer.parseInt(tasks.get(i).getId())) {
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

            if (endTime <= maxTimeToFinish && OutgoingCommsOK(taskEndTimes)) {
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

    @Override
    public int hashCode() {
        Map<String, Integer> nodeEndTime = new HashMap<>();

        for (String nodeId : visited.keySet()) {
            Pair<Integer, Integer> processorEndTime = visited.get(nodeId);

            nodeEndTime.put(nodeId, processorEndTime.getValue());
        }

        return Objects.hash(nodeEndTime);
    }
}
