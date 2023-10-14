package com.example.project2project2team16.searchers.comparators;

import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.SchedulingProblem;

import java.util.Comparator;

public class ScheduleNodeAStarComparator implements Comparator<ScheduleNode> {
    SchedulingProblem problem;

    public ScheduleNodeAStarComparator(SchedulingProblem problem) {
        this.problem = problem;
    }

    @Override
    public int compare(ScheduleNode node1, ScheduleNode node2) {
        int value1 = SchedulingProblem.CalculateF(node1);
        int value2 = SchedulingProblem.CalculateF(node2);

        if (value1 == value2) {
            if (node1.getNumTasksScheduled() >= node2.getNumTasksScheduled()) {
                return -1;
            } else {
                return 1;
            }
        } else if (value1 < value2) {
            return -1;
        } else {
            return 1;
        }
    }
}
