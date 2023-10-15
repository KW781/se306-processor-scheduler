package com.example.project2project2team16.searchers;

import java.util.Collections;
import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
    private Integer evalLimit = 0;
    private Integer nextEvalLimit = 0;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void initialiseSearcher() {
        super.initialiseSearcher();
        nextEvalLimit = problem.getStartNode().getfValue();
        evalLimit = problem.getStartNode().getfValue();
    }

    @Override
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            ScheduleNode newNode = newNodes.get(i);

            if (newNode.getfValue() <= evalLimit) {
                pruneOrAdd(newNode);
            }
            else {
                nextEvalLimit = Math.min(nextEvalLimit, newNode.getfValue());
            }
        }
    }

    @Override
    public ScheduleNode search() {
        ScheduleNode result = null;

        while (result == null) {
            result = super.search();
            createdSchedules.clear();
            evalLimit = nextEvalLimit;
            nextEvalLimit = Integer.MAX_VALUE;
            ScheduleNode startNode = problem.getStartNode();
            SchedulingProblem.initialiseF(startNode);
            addToFrontier(Collections.singletonList(startNode));
        }

        return result;
    }
}
