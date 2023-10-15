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
    private Set<ScheduleNode> createdSchedules = new HashSet<>();
    private int tasksVisited = 0;
    private int dups = 0;
    private int schedulesAdded = 0;
    private int schedulesExplored = 0;

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
        ScheduleNode startNode = getProblem().getStartNode();
        SchedulingProblem.initialiseF(startNode);

        super.initialiseSearcher();

        // Adds the first node to the GUI if visualisation is enabled
        GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
        helper.addNode(startNode, startNode.getParent());
        helper.setStartNode(startNode);
    }

    @Override
    protected void initialiseFrontier() {
        setFrontier(new PriorityQueue<>(new ScheduleNodeAStarComparator(getProblem())));
    }

    /**
     * Tries to prune the provided ScheduleNode.
     * If it is not detected as a duplicate, it is added to the frontier.
     * @param node The ScheduleNode to add.
     */
    protected void pruneOrAdd(ScheduleNode node) {
        // Tries to prune using Processor Normalisation technique
        if (getCreatedSchedules().contains(node)) {
            setDups(getDups() + 1);
            return;
        }

        // Tries to prune using Equivalent Schedule technique
        // If the ScheduleNode had a fixed task order at any point,
        // then Equivalent Schedule pruning is disabled.
        if (!node.isHadFixedTaskOrder()) {
            if (node.isEquivalent()) {
                setDups(getDups() + 1);
                return;
            }
        }

        setSchedulesAdded(getSchedulesAdded() + 1);
        getFrontier().add(node);
        getCreatedSchedules().add(node);
    }

    @Override
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            pruneOrAdd(newNode);
        }
    }

    @Override
    protected ScheduleNode getNextNode() {
        return ((PriorityQueue<ScheduleNode>) getFrontier()).poll();
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
            if (nextNode.getVisited().size() > getTasksVisited()) {
                GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
                helper.addNode(nextNode, nextNode.getParent());
                helper.updateOptimalNode(nextNode);
                setTasksVisited(nextNode.getVisited().size());
            }

            setSchedulesExplored(getSchedulesExplored() + 1);
            // If the ScheduleNode has visited all the tasks, immediately return as it is optimal.
            if (getProblem().isGoal(nextNode)) {
                return nextNode;
            }
            else {
                // Expanding and adding the neighbour states to the frontier.
                // If the ScheduleNode expands any unpromising children,
                // it is added back to the frontier. (Partial Expansion)
                addToFrontier(getProblem().getNeighbourStates(nextNode));
                if (nextNode.isUnpromisingChildren()) {
                    getFrontier().add(nextNode);
                    nextNode.setUnpromisingChildren(false);
                }
            }
        }

        return null;
    }

    public Set<ScheduleNode> getCreatedSchedules() {
        return createdSchedules;
    }

    public void setCreatedSchedules(Set<ScheduleNode> createdSchedules) {
        this.createdSchedules = createdSchedules;
    }

    public int getTasksVisited() {
        return tasksVisited;
    }

    public void setTasksVisited(int tasksVisited) {
        this.tasksVisited = tasksVisited;
    }

    public int getDups() {
        return dups;
    }

    public void setDups(int dups) {
        this.dups = dups;
    }

    public int getSchedulesAdded() {
        return schedulesAdded;
    }

    public void setSchedulesAdded(int schedulesAdded) {
        this.schedulesAdded = schedulesAdded;
    }

    public int getSchedulesExplored() {
        return schedulesExplored;
    }

    public void setSchedulesExplored(int schedulesExplored) {
        this.schedulesExplored = schedulesExplored;
    }
}
