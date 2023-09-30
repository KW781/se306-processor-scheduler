package com.example.project2project2team16.searchers.comparators;

import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.SchedulingProblem;

import java.util.Comparator;

public class ScheduleNodeUniformCostComparator  implements Comparator<ScheduleNode> {
    SchedulingProblem problem;

    public ScheduleNodeUniformCostComparator(SchedulingProblem problem) {
        this.problem = problem;
    }

    @Override
    public int compare(ScheduleNode node1, ScheduleNode node2) {
        Integer value1 = node1.GetPathCost();
        Integer value2 = node2.GetPathCost();

        if (value1 < value2) {
            return -1;
        } else if (value1 > value2) {
            return 1;
        }

        return 0;
    }
}
