package com.example.project2project2team16.searchers.comparators;

import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.SchedulingProblem;

import java.util.Comparator;
public class ScheduleNodeGreedyComparator implements Comparator<ScheduleNode> {
    SchedulingProblem problem;

    public ScheduleNodeGreedyComparator(SchedulingProblem problem) {
        this.problem = problem;
    }

    @Override
    public int compare(ScheduleNode node1, ScheduleNode node2) {
        Integer heuristic1 = SchedulingProblem.calculateF(node1);
        Integer heuristic2 = SchedulingProblem.calculateF(node2);

        if (heuristic1 < heuristic2) {
            return -1;
        } else if (heuristic1 > heuristic2) {
            return 1;
        }

        return 0;
    }
}
