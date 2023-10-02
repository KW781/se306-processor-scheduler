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
    Integer processorCount;
    ScheduleNode parent;

    public ScheduleNode(Integer processorCount, Set<Node> startingTasks) {
        this.availableTasks = startingTasks;
        this.processorCount = processorCount;
        visited = new HashMap<>();
        processorEndTimes = new ArrayList<>();

        for (int i = 0; i < processorCount; i++) {
            processorEndTimes.add(0);
        }
    }

    public ScheduleNode(ScheduleNode copy, Node newTask, Integer processor) {
        this.visited = new HashMap<>(copy.visited);
        this.availableTasks = new HashSet<>(copy.availableTasks);
        this.processorEndTimes = new ArrayList<>(copy.processorEndTimes);
        this.processorCount = copy.processorCount;
        this.parent = copy;

        AddTask(newTask, processor);
    }

    public Map<String, Pair<Integer, Integer>> GetVisited() {
        return visited;
    }

    public List<ScheduleNode> GenerateNeighbours() {
        List<ScheduleNode> neighbours = new ArrayList<>();

        for (Node task : availableTasks) {
            for (int i = 0; i < processorCount; i++) {
                neighbours.add(new ScheduleNode(this, task, i));
            }
        }

        return neighbours;
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
        availableTasks.remove(newTask);

        AddNewTasks(newTask);
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
}
