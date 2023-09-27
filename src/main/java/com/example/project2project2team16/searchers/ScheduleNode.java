package com.example.project2project2team16.searchers;

import javafx.util.Pair;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import java.util.*;

public class ScheduleNode {

    //Task Id as key, data is pair of processor run on and end time.
    Map<String, Pair<Integer, Integer>> visited;
    Set<Node> availableTasks;
    List<Integer> processorEndTimes;
    Integer processorCount;

    public ScheduleNode(Integer processorCount, Set<Node> startingTasks) {
        this.availableTasks = startingTasks;
        this.processorCount = processorCount;
        processorEndTimes = new ArrayList<>(processorCount);
        visited = new HashMap<>();
    }

    public ScheduleNode(ScheduleNode copy, Node newTask, Integer processor) {
        this.visited = new HashMap<>(copy.visited);
        this.availableTasks = new HashSet<>(copy.availableTasks);
        this.processorEndTimes = new ArrayList<>(copy.processorEndTimes);

        AddTask(newTask, processor);
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
}
