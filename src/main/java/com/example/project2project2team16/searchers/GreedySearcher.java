package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.comparators.ScheduleNodeGreedyComparator;

import java.util.PriorityQueue;

public class GreedySearcher extends DFSSearcher {

    public GreedySearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityQueue<ScheduleNode>(new ScheduleNodeGreedyComparator(problem));
    }

    @Override
    protected ScheduleNode GetNextNode() {
        return ((PriorityQueue<ScheduleNode>) frontier).poll();
    }
}
