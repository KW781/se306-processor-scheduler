package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.List;
import java.util.PriorityQueue;

public class AStarSearcher extends GreedySearcher {
    public AStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityQueue<ScheduleNode>(new ScheduleNodeAStarComparator(problem));
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        //Removed DFS pruning logic as multiple goal states won't be checked given that the heuristic is admissible

        frontier.addAll(newNodes);
    }

    @Override
    public ScheduleNode Search() {
        while (!IsFrontierEmpty()) {
            ScheduleNode nextNode = GetNextNode();

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
