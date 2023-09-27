package com.example.project2project2team16.searchers;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchedulingProblem {

    Graph taskGraph;
    ScheduleNode startingNode;
    Integer taskCount;

    public SchedulingProblem(Graph taskGraph) {
        this.taskGraph = taskGraph;
        this.taskCount = taskGraph.getNodeCount();

        GenerateStartNodes();
    }

    public ScheduleNode GetStartNode() {
        return startingNode;
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
