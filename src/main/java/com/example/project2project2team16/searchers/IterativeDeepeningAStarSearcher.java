package com.example.project2project2team16.searchers;

import java.util.Arrays;
import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
    Integer evalLimit = 0;
    Integer nextEvalLimit = 0;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void InitialiseSearcher() {
        super.InitialiseSearcher();
        nextEvalLimit = problem.GetStartNode().fValue;
        evalLimit = problem.GetStartNode().fValue;
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            Integer value = problem.CalculateF(newNodes.get(i));

            if (value <= evalLimit) {
                ScheduleNode newNode = newNodes.get(i);
                if (closed.contains(newNode) || opened.contains(newNode)) {
                    continue;
                }

                frontier.add(newNode);
                opened.add(newNode);
            }
            else {
                nextEvalLimit = Math.max(nextEvalLimit, value);
            }
        }
    }

    @Override
    public ScheduleNode Search() {
        ScheduleNode result = null;

        while (result == null) {
            result = super.Search();
            closed.clear();
            opened.clear();
            evalLimit = nextEvalLimit;
            AddToFrontier(Arrays.asList(problem.GetStartNode()));
        }

        return result;
    }
}
