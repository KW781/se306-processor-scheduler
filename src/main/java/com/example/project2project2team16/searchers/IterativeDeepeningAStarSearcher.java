package com.example.project2project2team16.searchers;

import java.util.Arrays;
import java.util.Collections;
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
                if (createdSchedules.contains(newNode) || newNode.IsEquivalent()) {
                    dups++;
                    continue;
                }

                schedulesAdded++;
                frontier.add(newNode);
                createdSchedules.add(newNode);
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
            createdSchedules.clear();
            evalLimit = nextEvalLimit;
            ScheduleNode startNode = problem.GetStartNode();
            SchedulingProblem.initialiseF(startNode);
            AddToFrontier(Collections.singletonList(startNode));
            schedulesAdded = 0;
            dups = 0;
            schedulesExplored = 0;
        }

        return result;
    }
}
