package com.example.project2project2team16.searchers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/*
 * Basic Searcher producing an optional solution using DFS. Can be extended from for more advanced search algorithms.
 */
public class DFSSearcher {
    SchedulingProblem problem;

    //Needs to be list type in the future.
    Stack<ScheduleNode> frontier;
    ScheduleNode optimal = null;
    Integer currentOptimalTime;

    public DFSSearcher(SchedulingProblem problem) {
        this.problem = problem;
        InitialiseFrontier();
        AddToFrontier(Arrays.asList(problem.GetStartNode()));
    }

    //Exhaustive search
    public ScheduleNode Search() {

        while (!IsFrontierEmpty()) {
            ScheduleNode nextNode = frontier.peek();
            frontier.pop();

            if (problem.IsGoal(nextNode)) {
                UpdateOptimal(nextNode);
            }
            else {
                AddToFrontier(problem.GetNeighbourStates(nextNode));
            }
        }

        return optimal;
    }

    private void InitialiseFrontier() {
        frontier = new Stack<ScheduleNode>();
    }

    private boolean IsFrontierEmpty() {
        return frontier.isEmpty();
    }

    private void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            if (optimal == null || newNodes.get(i).GetValue() < currentOptimalTime) {
                frontier.add(newNodes.get(i));
            }
        }
    }

    private void UpdateOptimal(ScheduleNode newSolution) {
        Integer newSolutionTime = newSolution.GetValue();

        if (optimal == null || newSolutionTime < currentOptimalTime) {
            optimal = newSolution;
            currentOptimalTime = newSolutionTime;
        }
    }
}
