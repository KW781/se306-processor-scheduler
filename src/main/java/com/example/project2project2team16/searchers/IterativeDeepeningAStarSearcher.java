package com.example.project2project2team16.searchers;

import java.util.Arrays;
import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
    Integer evalLimit = 0;
    Integer nextEvalLimit = Integer.MAX_VALUE;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void InitialiseSearcher() {
        super.InitialiseSearcher();
        evalLimit = problem.GetStartNode().fValue;
    }

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

                schedulesAdded++;
                frontier.add(newNode);
                opened.add(newNode);
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
            closed.clear();
            opened.clear();
            evalLimit = nextEvalLimit;
            nextEvalLimit = Integer.MAX_VALUE;
            System.out.println(schedulesAdded + " schedules added");
            System.out.println(dups + " duplicates detected");
            System.out.println(explored + " schedules explored");
            System.out.println("-----NEXT ITERATION-----");
            schedulesAdded = 0;
            dups = 0;
            explored = 0;
            AddToFrontier(Arrays.asList(problem.GetStartNode()));
        }

        return result;
    }
}
