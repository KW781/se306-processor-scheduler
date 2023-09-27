package com.example.project2project2team16.searchers;

import javafx.util.Pair;

import java.util.*;

public class ScheduleNode {

    //Task Id as key, data is pair of processor run on and end time.
    Map<String, Pair<Integer, Integer>> visited;
    Set<TaskNode> availableTasks;
    List<Integer> processorEndTimes;
    Integer processorCount;

    public ScheduleNode(Integer processorCount) {
        this.processorCount = processorCount;
        processorEndTimes = new ArrayList<>(processorCount);
        visited = new HashMap<>();
    }

    public ScheduleNode(ScheduleNode copy, TaskNode newTask, Integer processor) {
        this.visited = copy.visited;
        this.processorEndTimes = copy.processorEndTimes;

        AddTask(newTask, processor);
    }

    public List<ScheduleNode> GenerateNeighbours() {
        List<ScheduleNode> neighbours = new ArrayList<>();

        for (TaskNode task : availableTasks) {
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

    private void AddTask(TaskNode newTask, Integer processor) {
        //TODO
    }



}
