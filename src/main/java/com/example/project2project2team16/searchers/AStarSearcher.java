package com.example.project2project2team16.searchers;

import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;

/**
 * A searcher which utilises the A* searching algorithm
 */
public class AStarSearcher extends GreedySearcher {
    Set<ScheduleNode> createdSchedules = new HashSet<>();
    int tasksVisited = 0;
    int dups = 0;
    int schedulesAdded = 0;
    int schedulesExplored = 0;

    /**
     * Constructor which sets problem for the search
     *
     * @param problem The Scheduling Problem
     */
    public AStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void initialiseSearcher() {
        // Initialises the F value for the starting node
        ScheduleNode startNode = problem.getStartNode();
        SchedulingProblem.initialiseF(startNode);

        super.initialiseSearcher();

        // Adds the first node to the GUI if visualisation is enabled
        GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
        helper.addNode(startNode, startNode.parent);
        helper.setStartNode(startNode);
    }

    @Override
    protected void initialiseFrontier() {
        frontier = new PriorityQueue<>(new ScheduleNodeAStarComparator(problem));
    }

    /**
     * Tries to prune the provided ScheduleNode.
     * If it is not detected as a duplicate, it is added to the frontier.
     * @param node The ScheduleNode to add.
     */
    protected void pruneOrAdd(ScheduleNode node) {
        // Tries to prune using Processor Normalisation technique
        if (createdSchedules.contains(node)) {
            dups++;
            return;
        }

        // Tries to prune using Equivalent Schedule technique
        // If the ScheduleNode had a fixed task order at any point,
        // then Equivalent Schedule pruning is disabled.
        if (!node.hadFixedTaskOrder) {
            if (node.isEquivalent()) {
                dups++;
                return;
            }
        }

        schedulesAdded++;
        frontier.add(node);
        createdSchedules.add(node);
    }

    @Override
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            pruneOrAdd(newNode);
        }
    }

    @Override
    protected ScheduleNode getNextNode() {
        return ((PriorityQueue<ScheduleNode>) frontier).poll();
    }

    /**
     * Performs a search using the A* search algorithm.
     * @return The optimal schedule, or null if none is found.
     */
    @Override
    public ScheduleNode search() {
        while (!isFrontierEmpty()) {
            ScheduleNode nextNode = getNextNode();
            // Checks if the ScheduleNode has visited more tasks than the previous maximum tasks visited
            // If true, it is considered the next best partial schedule
            // Therefore it is added to the GUI for visualisation
            if (nextNode.visited.size() > tasksVisited) {
                GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
                helper.addNode(nextNode, nextNode.parent);
                helper.updateOptimalNode(nextNode);
                tasksVisited = nextNode.visited.size();
            }

            schedulesExplored++;
            // If the ScheduleNode has visited all the tasks, immediately return as it is optimal.
            if (problem.isGoal(nextNode)) {
                return nextNode;
            }
            else {
                // Expanding and adding the neighbour states to the frontier.
                // If the ScheduleNode expands any unpromising children,
                // it is added back to the frontier. (Partial Expansion)
                addToFrontier(problem.getNeighbourStates(nextNode));
                if (nextNode.unpromisingChildren) {
                    frontier.add(nextNode);
                    nextNode.unpromisingChildren = false;
                }
            }
        }

        return null;
    }
}
