package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;

public class AStarSearcher extends GreedySearcher {
    Set<ScheduleNode> opened = new HashSet<>();
    Set<ScheduleNode> closed = new HashSet<>();
    int tasksVisited = 0;

    public AStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void InitialiseSearcher() {
        ScheduleNode startNode = problem.GetStartNode();
        problem.initialiseF(startNode);

        super.InitialiseSearcher();

        GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
        helper.addNode(startNode, startNode.parent);
        helper.setStartNode(startNode);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityQueue<ScheduleNode>(new ScheduleNodeAStarComparator(problem));
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            ScheduleNode newNode = newNodes.get(i);
            if (closed.contains(newNode) || opened.contains(newNode)) {
                continue;
            }

            frontier.add(newNode);
            opened.add(newNode);
        }
    }

    @Override
    protected ScheduleNode GetNextNode() {
        ScheduleNode node = ((PriorityQueue<ScheduleNode>) frontier).poll();
        closed.add(node);
        opened.remove(node);

        return node;
    }

    @Override
    public ScheduleNode Search() {
        while (!IsFrontierEmpty()) {
            ScheduleNode nextNode = GetNextNode();
            if (nextNode.visited.size() > tasksVisited) {
                GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
                helper.addNode(nextNode, nextNode.parent);
                helper.updateOptimalNode(nextNode);
                tasksVisited = nextNode.visited.size();
            }

            if (problem.IsGoal(nextNode)) {
                return nextNode;
            }
            else {
                AddToFrontier(problem.GetNeighbourStates(nextNode));
            }
        }

        return null;
    }
}
