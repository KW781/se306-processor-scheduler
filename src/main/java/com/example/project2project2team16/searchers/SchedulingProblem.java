package com.example.project2project2team16.searchers;

import java.util.List;

public class SchedulingProblem {
    List<ScheduleNode> startingNodes;
    Integer taskCount;

    public SchedulingProblem(List<TaskNode> startingTasks, Integer taskNumber) {
        GenerateStartingNodes(startingTasks);
        this.taskCount = taskNumber;
    }

    public List<ScheduleNode> GetStartNodes() {
        return startingNodes;
    }

    public boolean IsGoal(ScheduleNode node) {
        return node.IsComplete(taskCount);
    }

    public List<ScheduleNode> GetNeighbourStates(ScheduleNode node) {
        return node.GenerateNeighbours();
    }

    public Integer Heuristic(ScheduleNode node) {
        //TODO
        return 0;
    }

}
