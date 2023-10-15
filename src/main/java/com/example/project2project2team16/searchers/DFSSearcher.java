package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;

import java.util.*;

/*
 * Basic Searcher producing an optional solution using DFS. Can be extended from for more advanced search algorithms.
 */
public class DFSSearcher {
    private SchedulingProblem problem;
    private Collection<ScheduleNode> frontier;
    private ScheduleNode optimal = null;
    private Integer currentOptimalTime;

    /**
     * Constructor which sets problem for the search
     *
     * @param problem The Scheduling Problem
     */
    public DFSSearcher(SchedulingProblem problem) {
        this.setProblem(problem);
    }

    /**
     * Initialises variables required for the search
     */
    public void initialiseSearcher() {
        initialiseFrontier();
        addToFrontier(Collections.singletonList(getProblem().getStartNode()));
    }

    /**
     * Performs an exhaustive search on the provided SchedulingProblem.
     * @return The most optimal solution
     */
    public ScheduleNode search() {

        while (!isFrontierEmpty()) {
            ScheduleNode nextNode = getNextNode();

            if (getProblem().isGoal(nextNode)) {
                updateOptimal(nextNode);
            }
            else {
                addToFrontier(getProblem().getNeighbourStates(nextNode));
            }
        }

        return getOptimal();
    }

    /**
     * @return The next Schedule Node to expand.
     */
    protected ScheduleNode getNextNode() {
        ScheduleNode nextNode = ((Stack<ScheduleNode>) getFrontier()).peek();
        ((Stack<ScheduleNode>) getFrontier()).pop();

        return nextNode;
    }

    /**
     * Initialises the frontier to prepare for searching.
     */
    protected void initialiseFrontier() {
        setFrontier(new Stack<ScheduleNode>());
    }

    /**
     * @return True if frontier is empty, false otherwise.
     */
    protected boolean isFrontierEmpty() {
        return getFrontier().isEmpty();
    }

    /**
     * Adds a list of ScheduleNodes to the frontier.
     * @param newNodes The list of new ScheduleNodes.
     */
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            if (getOptimal() == null || newNodes.get(i).getValue() < getCurrentOptimalTime()) {
                getFrontier().add(newNodes.get(i));
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

        if (getOptimal() == null || newSolutionTime < getCurrentOptimalTime()) {
            GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
            helper.addNode(newSolution, getOptimal());
            helper.updateOptimalNode(newSolution);
            setOptimal(newSolution);
            setCurrentOptimalTime(newSolutionTime);
        }
    }

    public SchedulingProblem getProblem() {
        return problem;
    }

    public void setProblem(SchedulingProblem problem) {
        this.problem = problem;
    }

    public Collection<ScheduleNode> getFrontier() {
        return frontier;
    }

    public void setFrontier(Collection<ScheduleNode> frontier) {
        this.frontier = frontier;
    }

    public ScheduleNode getOptimal() {
        return optimal;
    }

    public void setOptimal(ScheduleNode optimal) {
        this.optimal = optimal;
    }

    public Integer getCurrentOptimalTime() {
        return currentOptimalTime;
    }

    public void setCurrentOptimalTime(Integer currentOptimalTime) {
        this.currentOptimalTime = currentOptimalTime;
    }
}
