package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeUniformCostComparator;

import java.util.PriorityQueue;

public class UniformCostSearcher extends AStarSearcher {

    public UniformCostSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityQueue<ScheduleNode>(new ScheduleNodeUniformCostComparator(problem));
    }
}
