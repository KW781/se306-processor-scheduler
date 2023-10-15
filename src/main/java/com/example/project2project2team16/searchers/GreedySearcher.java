package com.example.project2project2team16.searchers;

import com.example.project2project2team16.searchers.comparators.ScheduleNodeGreedyComparator;

import java.util.PriorityQueue;

/**
 * A Greedy Searcher which utilises a Greedy comparator to perform the search
 */
public class GreedySearcher extends DFSSearcher {

    /**
     * Constructor which sets problem for the search
     *
     * @param problem The Scheduling Problem
     */
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
