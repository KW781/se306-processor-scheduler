package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;

public class AStarSearcher extends GreedySearcher {
    Set<ScheduleNode> createdSchedules = new HashSet<>();
    int tasksVisited = 0;
    int dups = 0;
    int schedulesAdded = 0;
    int schedulesExplored = 0;
    public static int created = 0;

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
        frontier = new PriorityQueue<>(new ScheduleNodeAStarComparator(problem));
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            if (createdSchedules.contains(newNode)) {
                dups++;
                continue;
            }

            schedulesAdded++;
            SchedulingProblem.CalculateF(newNode);
            frontier.add(newNode);
            createdSchedules.add(newNode);
        }
    }

    @Override
    protected ScheduleNode GetNextNode() {
        return ((PriorityQueue<ScheduleNode>) frontier).poll();
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

            schedulesExplored++;
            if (problem.IsGoal(nextNode)) {
                System.out.println(created + " schedules created");
                System.out.println(schedulesAdded + " schedules added");
                System.out.println(dups + " duplicates detected");
                System.out.println(schedulesExplored + " schedules explored");
                return nextNode;
            }
            else {
                AddToFrontier(problem.GetNeighbourStates(nextNode));
                if (nextNode.unpromisingChildren) {
                    frontier.add(nextNode);
                    nextNode.unpromisingChildren = false;
                }
            }
        }

        return null;
    }
}
