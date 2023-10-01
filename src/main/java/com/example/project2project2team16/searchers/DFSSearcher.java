package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/*
 * Basic Searcher producing an optional solution using DFS. Can be extended from for more advanced search algorithms.
 */
public class DFSSearcher {
    SchedulingProblem problem;
    Collection<ScheduleNode> frontier;
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
            ScheduleNode nextNode = GetNextNode();

            if (problem.IsGoal(nextNode)) {
                UpdateOptimal(nextNode);
            }
            else {
                AddToFrontier(problem.GetNeighbourStates(nextNode));
            }
        }

        return optimal;
    }

    protected ScheduleNode GetNextNode() {
        ScheduleNode nextNode = ((Stack<ScheduleNode>) frontier).peek();
        ((Stack<ScheduleNode>) frontier).pop();

        return nextNode;
    }

    protected void InitialiseFrontier() {
        frontier = new Stack<ScheduleNode>();
    }

    protected boolean IsFrontierEmpty() {
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
            GraphVisualisationHelper.addNode(newSolution, optimal);
            GraphVisualisationHelper.updateOptimalNode(newSolution);
            optimal = newSolution;
            currentOptimalTime = newSolutionTime;
            System.out.println(currentOptimalTime);
        }
    }
}
