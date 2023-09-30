package com.example.project2project2team16.searchers;

import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
    Integer evalLimit = 0;
    Integer nextEvalLimit = Integer.MAX_VALUE;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            Integer value = problem.Heuristic(newNodes.get(i)) + newNodes.get(i).GetPathCost();

            if (value < evalLimit) {
                frontier.add(newNodes.get(i));
            }
            else {
                nextEvalLimit = Math.min(nextEvalLimit, value);
            }
        }
    }

    @Override
    public ScheduleNode Search() {
        ScheduleNode result = null;

        while (result == null) {
            result = super.Search();
        }

        return result;
    }
}
