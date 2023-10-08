package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
//    Integer evalLimit = 0;
//    Integer nextEvalLimit = Integer.MAX_VALUE;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }
    int j = 0;
    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            Integer value = problem.CalculateF(newNodes.get(i));

            if (value <= evalLimit) {
                ScheduleNode newNode = newNodes.get(i);
                if (closed.contains(newNode) || opened.contains(newNode)) {
                    dups++;
                    continue;
                }

                frontier.add(newNode);
                opened.add(newNode);
            }
            else {
                j++;
                nextEvalLimit = Math.min(nextEvalLimit, value);
            }
        }
    }

    @Override
    public ScheduleNode Search() {
        ScheduleNode result = null;

        while (result == null) {
            result = super.Search();
            AddToFrontier(Arrays.asList(problem.GetStartNode()));
            closed.clear();
            opened.clear();
            evalLimit = nextEvalLimit;
            nextEvalLimit = Integer.MAX_VALUE;
        }

        return result;
    }
}
