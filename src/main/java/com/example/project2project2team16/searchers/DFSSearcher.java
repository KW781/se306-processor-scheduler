package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;

import java.util.*;

/*
 * Basic Searcher producing an optional solution using DFS. Can be extended from for more advanced search algorithms.
 */
public class DFSSearcher {
    protected SchedulingProblem problem;
    protected Collection<ScheduleNode> frontier;
    private ScheduleNode optimal = null;
    private Integer currentOptimalTime;

    /**
     * Constructor which sets problem for the search
     *
     * @param problem The Scheduling Problem
     */
    public DFSSearcher(SchedulingProblem problem) {
        this.problem = problem;
    }

    /**
     * Initialises variables required for the search
     */
    public void initialiseSearcher() {
        initialiseFrontier();
        addToFrontier(Collections.singletonList(problem.getStartNode()));
    }

    /**
     * Performs an exhaustive search on the provided SchedulingProblem.
     * @return The most optimal solution
     */
    public ScheduleNode search() {

        while (!isFrontierEmpty()) {
            ScheduleNode nextNode = getNextNode();

            if (problem.isGoal(nextNode)) {
                updateOptimal(nextNode);
            }
            else {
                addToFrontier(problem.getNeighbourStates(nextNode));
            }
        }

        return optimal;
    }

    /**
     * @return The next Schedule Node to expand.
     */
    protected ScheduleNode getNextNode() {
        ScheduleNode nextNode = ((Stack<ScheduleNode>) frontier).peek();
        ((Stack<ScheduleNode>) frontier).pop();

        return nextNode;
    }

    /**
     * Initialises the frontier to prepare for searching.
     */
    protected void initialiseFrontier() {
        frontier = new Stack<ScheduleNode>();
    }

    /**
     * @return True if frontier is empty, false otherwise.
     */
    protected boolean isFrontierEmpty() {
        return frontier.isEmpty();
    }

    /**
     * Adds a list of ScheduleNodes to the frontier.
     * @param newNodes The list of new ScheduleNodes.
     */
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            if (optimal == null || newNodes.get(i).getValue() < currentOptimalTime) {
                frontier.add(newNodes.get(i));
            }
        }
    }

    /**
     * Checks if the provided solution is better than the current most optimal solution found.
     * Updates the most optimal if true, otherwise does nothing.
     * @param newSolution A completed solution.
     */
    private void updateOptimal(ScheduleNode newSolution) {
        Integer newSolutionTime = newSolution.getValue();

        if (optimal == null || newSolutionTime < currentOptimalTime) {
            GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
            helper.addNode(newSolution, optimal);
            helper.updateOptimalNode(newSolution);
            optimal = newSolution;
            currentOptimalTime = newSolutionTime;
        }
    }
}
