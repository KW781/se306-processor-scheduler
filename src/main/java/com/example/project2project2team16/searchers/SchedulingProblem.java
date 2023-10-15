package com.example.project2project2team16.searchers;

import com.example.project2project2team16.exceptions.PreqrequisiteNotMetException;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.enums.Heuristic;
import javafx.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains all information about the provided scheduling problem
 */
public class SchedulingProblem {

    private static Graph taskGraph;
    private ScheduleNode startingNode;
    private Integer taskCount;
    private static Integer processorCount;
    private static Integer computationCostSum;
    private static Map<Heuristic, Integer> heuristicCount;
    private static Map<String, Integer> dfsMemo;

    public SchedulingProblem(Graph taskGraph, int processorCount) {
        this.taskGraph = taskGraph;
        this.taskCount = taskGraph.getNodeCount();
        this.processorCount = processorCount;
        dfsMemo = new HashMap<>();

        this.pruneDuplicateTasks();

        // initialise the heuristic count hashmap for all heuristics available
        this.heuristicCount = new HashMap<>();
        for (Heuristic heuristic : Heuristic.values()) {
            this.heuristicCount.put(heuristic, 0);
        }

        Set<Node> startingTasks = generateStartingTasks();

        startingNode = new ScheduleNode(processorCount, startingTasks);

        calculateComputationCostSum(taskGraph);
    }

    /**
     * Calculates the computation cost sum of the provided task graph.
     * @param taskGraph the task graph for calculate for.
     */
    private void  calculateComputationCostSum(Graph taskGraph) {
        computationCostSum = 0;
        for (Node node : taskGraph) {
            computationCostSum += node.getAttribute("Weight", Double.class).intValue();
        }
    }

    private void addVirtualEdge(Node node1, Node node2) {
        if (node1.getIndex() < node2.getIndex()) {
            Edge newEdge = taskGraph.addEdge(node1.getIndex() + "virtual"  + node2.getIndex(), node1, node2, true);
            newEdge.setAttribute("Weight", 0.0);
        } else {
            Edge newEdge = taskGraph.addEdge(node2.getIndex() + "virtual" + node1.getIndex(), node1, node2, true);
            newEdge.setAttribute("Weight", 0.0);
        }
    }

    /**
     * Searches for duplicate tasks in the task graph and prunes them by enforcing a fixed order in which they are explored.
     */
    private void pruneDuplicateTasks() {
        // get nodes that have no incoming edges
        List<Node> rootNodes = this.taskGraph.nodes().filter(node -> node.getInDegree() == 0).collect(Collectors.toList());
        Set<Node> visited = new HashSet<>();
        Queue<Node> nodeQueue = new ArrayDeque<>();
        boolean allNodesVisited = false;

        Map<Node, List<Node>> equivalentNodesMap = new HashMap<>();
        // check for equivalent tasks between root nodes
        for (int i = 0; i < rootNodes.size(); i++) {
            Node outerNode = rootNodes.get(i);
            for (int j = i + 1; j < rootNodes.size(); j++) {
                Node innerNode = rootNodes.get(j);
                if (areTasksEquivalent(outerNode, innerNode)) {
                    // if the tasks are equivalent, add a directed edge between the two with a weight of zero
                    // the edges are formed in ascending index order to not conflict with equivalent schedule pruning
                    addVirtualEdge(outerNode, innerNode);
                }
            }
        }

        // run BFS to find duplicate tasks
        nodeQueue.add(rootNodes.get(0));
        while (!allNodesVisited) {
            while (!nodeQueue.isEmpty()) {
                Node currentNode = nodeQueue.remove();
                visited.add(currentNode); // mark the current node as visited so that we don't revisit it
                List<Node> childNodes = currentNode.leavingEdges().filter(edge -> !edge.getId().contains("virtual")).map(edge -> edge.getTargetNode()).collect(Collectors.toList());

                // compare child nodes with each other to see if they are duplicates
                for (int i = 0; i < childNodes.size(); i++) {
                    Node outerChildNode = childNodes.get(i);
                    for (int j = i + 1; j < childNodes.size(); j++) {
                        Node innerChildNode = childNodes.get(j);
                        // only compare the child nodes if at least one of them has not yet been visited
                        if (!(visited.contains(outerChildNode) && visited.contains(innerChildNode))) {
                            if (areTasksEquivalent(outerChildNode, innerChildNode)) {
                                // if the tasks are equivalent, add a directed edge between the two with a weight of zero
                                // the edges are formed in ascending index order to not conflict with equivalent schedule pruning
                                addVirtualEdge(outerChildNode, innerChildNode);
                            }
                        }
                    }

                    // add the child node to the queue if we haven't visited it
                    if (!visited.contains(outerChildNode)) nodeQueue.add(outerChildNode);
                }
            }

            // add any root nodes that we haven't visited so that we can perform the rooted search and completed BFS
            allNodesVisited = true;
            for (Node rootNode : rootNodes) {
                if (!visited.contains(rootNode)) {
                    allNodesVisited = false;
                    nodeQueue.add(rootNode);
                    break;
                }
            }
        }
    }

    private boolean areTasksEquivalent(Node task1, Node task2) {
        // check that the weights of the tasks are the same
        if (task1.getAttribute("Weight", Double.class).intValue() != task2.getAttribute("Weight", Double.class).intValue()) {
            return false;
        }


        // check that the parents of the tasks are the same, and that edges from the parents to the tasks have the same weight
        List<Edge> task1IncomingEdges = task1.enteringEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());
        List<Edge> task2IncomingEdges = task2.enteringEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());
        if (task1IncomingEdges.size() != task2IncomingEdges.size()) return false;

        boolean sameEdgeAndParentFound;
        for (Edge task1IncomingEdge : task1IncomingEdges) {
            sameEdgeAndParentFound = false;
            for (Edge task2IncomingEdge : task2IncomingEdges) {
                sameEdgeAndParentFound = true;
                if (task1IncomingEdge.getAttribute("Weight", Double.class).intValue() != task2IncomingEdge.getAttribute("Weight", Double.class).intValue()) {
                    sameEdgeAndParentFound = false;
                    continue;
                }
                if (task1IncomingEdge.getSourceNode() != task2IncomingEdge.getSourceNode()) {
                    sameEdgeAndParentFound = false;
                }
            }
            // tasks are not equivalent if we cannot find an equivalent edge for task 1 in task 2
            if (!sameEdgeAndParentFound) return false;
        }


        // check that the children of the tasks are the same and that the edges from the tasks to the children have the same weight
        List<Edge> task1OutgoingEdges = task1.leavingEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());
        List<Edge> task2OutgoingEdges = task2.leavingEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());
        if (task1OutgoingEdges.size() != task2OutgoingEdges.size()) return false;

        boolean sameEdgeAndChildFound;
        for (Edge task1OutgoingEdge : task1OutgoingEdges) {
            sameEdgeAndChildFound = false;
            for (Edge task2OutgoingEdge : task2OutgoingEdges) {
                sameEdgeAndChildFound = true;
                if (task1OutgoingEdge.getAttribute("Weight", Double.class).intValue() != task2OutgoingEdge.getAttribute("Weight", Double.class).intValue()) {
                    sameEdgeAndChildFound = false;
                    continue;
                }
                if (task1OutgoingEdge.getTargetNode() != task2OutgoingEdge.getTargetNode()) {
                    sameEdgeAndChildFound = false;
                }
            }
            // tasks are not equivalent if we cannot find an equivalent edge for task 1 in task 2
            if (!sameEdgeAndChildFound) return false;
        }

        return true;
    }

    public ScheduleNode getStartNode() {
        return startingNode;
    }

    /**
     * Checks if a ScheduleNode has scheduled all required tasks.
     * @param node ScheduleNode to check
     * @return true if goal, false otherwise
     */
    public boolean isGoal(ScheduleNode node) {
        return node.isComplete(taskCount);
    }

    /**
     * Returns a list of all the relevant neighbouring states of the specified ScheduleNode
     * Need to check how the ScheduleNode generates neighbours, as it may require extra work
     * after generation.
     *
     * @param node the ScheduleNode to expand
     * @return list of all relevant neighbouring states
     */
    public List<ScheduleNode> getNeighbourStates(ScheduleNode node) {
        return node.generateNeighbours();
    }

    /**
     * Calculates the F value of the specified ScheduleNode by taking
     * the max of multiple heuristics.
     *
     * @param node The ScheduleNode to calculate for.
     * @return the F value of the ScheduleNode.
     */
    public static Integer calculateF(ScheduleNode node) {
        // If F value already calculated, no need to recalculate
        if (node.getfValue() != 0) {
            return node.getfValue();
        }

        int loadBalanceHeuristic = loadBalanceHeuristic(node);
        // DRT heuristic is currently commented out as it is not improving runtimes
        // However, it will be used during visualisation, where runtime doesn't matter
        int dataReadyTimeHeuristic = GraphVisualisationHelper.instance().getGraph() == null ? 0 : dataReadyTimeHeuristic(node);
        int bottomLevelHeuristic = bottomLevelHeuristic(node);

        int maxHeuristic = Math.max(Math.max(loadBalanceHeuristic, bottomLevelHeuristic), dataReadyTimeHeuristic);

        // Updating which heuristic is used for visualisation information
        if (maxHeuristic == loadBalanceHeuristic) {
            heuristicCount.replace(Heuristic.IDLE_TIME, heuristicCount.get(Heuristic.IDLE_TIME) + 1);
            node.setHeuristicUsed(Heuristic.IDLE_TIME);
        } else if (maxHeuristic == dataReadyTimeHeuristic) {
            heuristicCount.replace(Heuristic.DATA_READY, heuristicCount.get(Heuristic.DATA_READY) + 1);
            node.setHeuristicUsed(Heuristic.DATA_READY);
        } else {
            heuristicCount.replace(Heuristic.BOTTOM_LEVEL, heuristicCount.get(Heuristic.BOTTOM_LEVEL) + 1);
            node.setHeuristicUsed(Heuristic.BOTTOM_LEVEL);

        }

        // Set and return the calculated F value
        node.setfValue(maxHeuristic);
        return maxHeuristic;
    }

    /**
     * Performs dfs to calculate the Critical Computational Path cost of the specified node.
     * @param node Node to calculate for.
     * @return Critical Computational Path cost.
     */
    private static int dfs(Node node) {
        if (dfsMemo.containsKey(node.getId())) {
            return dfsMemo.get(node.getId());
        }

        int cost = 0;

        // For each child, calculate the maximum computational path cost and store the maximum
        // We ignore the "Virtual" edges as they are only used when deciding whether to add the task to availableTasks
        // and not for anything else
        List<Node> nodeChildren = node.leavingEdges().filter(edge -> !edge.getId().contains("virtual")).map(Edge::getTargetNode).collect(Collectors.toList());
        for (Node child : nodeChildren) {
            int childCost = dfs(child);

            cost = Math.max(cost, childCost);
        }

        // Add current node's computational cost to maximum child cost
        cost += node.getAttribute("Weight", Double.class).intValue();
        dfsMemo.put(node.getId(), cost);

        return cost;
    }

    /**
     * Calculates the Critical Computational Path cost of the specified node, excluding the
     * computational cost of the node.
     * @param node The node to calculate for.
     * @return Critical Computational Path cost excl. node cost.
     */
    private static int getCriticalPath(Node node) {
        return dfs(node) - node.getAttribute("Weight", Double.class).intValue();
    }

    /**
     * Initialise the F value for a ScheduleNode with no scheduled tasks.
     * @param node The ScheduleNode to initialise.
     */
    public static void initialiseF(ScheduleNode node) {
        int bottomLevelHeuristic = 0;
        for (Node task : node.getAvailableTasks()) {
            int cp = getCriticalPath(task) + task.getAttribute("Weight", Double.class).intValue();
            bottomLevelHeuristic = Math.max(bottomLevelHeuristic, cp);
        }

        node.setfValue(bottomLevelHeuristic);
        heuristicCount.replace(Heuristic.BOTTOM_LEVEL, heuristicCount.get(Heuristic.BOTTOM_LEVEL) + 1);
        node.setHeuristicUsed(Heuristic.BOTTOM_LEVEL);
    }

    /**
     * Calculates the Bottom Level Heuristic for the provided ScheduleNode.
     * @param node The ScheduleNode to calculate for.
     * @return The Bottom Level heuristic.
     */
    public static Integer bottomLevelHeuristic(ScheduleNode node) {
        // The F value is defined as f(n) = g(n) + h(n)
        // g(n) is the total cost of the path from the root node to n
        // h(n) is the estimated critical computational path starting from n
        if (node.getfValue() != 0) {
            return node.getfValue();
        }

        if (node.isComplete(taskGraph.getNodeCount())) {
            return node.getValue();
        }

        int cost = 0;

        // If parent is not null, no need to recalculate bottom level for all tasks
        // Only recalculate for the last task added.
        if (node.getParent() != null) {
            if (node.getParent().getfValue() != 0) {
                int cp = getCriticalPath(node.getLastTask());

                cost = Math.max(node.getParent().getfValue(), cp + node.getProcessorPathCost(node.getLastProcessor()));

                return cost;
            }
        }

        // If no parent, we calculate bottom level for the last tasks of each processor,
        // taking the maximum as our cost.
        List<Integer> processorEndTimes = node.getProcessorEndTimes();
        List<Node> processorLastTasks = node.getProcessorLastTasks();
        for (int i = 0; i < node.getProcessorCount(); i++) {
            Node n = processorLastTasks.get(i);

            if (n == null) {
                continue;
            }

            int cp = getCriticalPath(n) + processorEndTimes.get(i);
            cost = Math.max(cost, cp);
        }

        return cost;
    }

    /**
     * Calculates the DRT Heuristic for the specified ScheduleNode.
     * @param node The ScheduleNode to calculate for.
     * @return The DRT heuristic.
     */
    private static int dataReadyTimeHeuristic(ScheduleNode node) {
        int maxDRTHeuristic = 0;
        int minDRT; // the minimum DRT across all processors

        for (Node currentNode : node.getAvailableTasks()) {
            try {
                minDRT = calculateMaxDRT(currentNode, 0, node.getVisited());
                for (int i = 1; i < node.getProcessorCount(); i++) {
                    minDRT = Math.min(minDRT, calculateMaxDRT(currentNode, i, node.getVisited()));
                }
                maxDRTHeuristic = Math.max(maxDRTHeuristic, minDRT + getCriticalPath(currentNode));
            } catch (PreqrequisiteNotMetException ignored) {
            }
        }

        return maxDRTHeuristic;
    }

    /**
     * Calculates the maximum DRT for the specified task on the specified processor.
     *
     * @param taskNode The task to calculate for.
     * @param processor The processor to schedule the task on.
     * @param visited The visited tasks of the ScheduleNode.
     * @return The maximum DRT of the task.
     */
    public static int calculateMaxDRT(Node taskNode, Integer processor, Map<String, Pair<Integer, Integer>> visited) {
        int maxDRT = 0;
        List<Edge> incomingEdges = taskNode.enteringEdges().filter(edge -> !edge.getId().contains("virtual")).collect(Collectors.toList());
        int finishTime;

        // For each parent, we check if the prerequisite has been met.
        // If met, we get calculate its DRT by doing Finish Time + Edge Weight.
        for (Edge incomingEdge : incomingEdges) {
            String prereqTaskId = incomingEdge.getSourceNode().getId();
            if (!visited.containsKey(prereqTaskId)) throw new PreqrequisiteNotMetException();
            finishTime = visited.get(prereqTaskId).getValue();
            finishTime += visited.get(prereqTaskId).getKey() == processor ? 0 : incomingEdge.getAttribute("Weight", Double.class).intValue();
            maxDRT = Math.max(maxDRT, finishTime);
        }

        return maxDRT;
    }

    /**
     * Calculates the Load Balance Heuristic for the specified ScheduleNode.
     *
     * @param node The ScheduleNode to calculate for.
     * @return The Load Balance Heuristic.
     */
    private static int loadBalanceHeuristic(ScheduleNode node) {
        return (computationCostSum + node.getIdleTime() + calculateTrailingIdleTimes(node))/ processorCount;
    }

    //Makes the idle time heuristic work on its own but increases runtime when paired with bottom level
    private static int calculateTrailingIdleTimes(ScheduleNode node) {
        int trailingTime = 0;
        int completedDuration = node.getCompletedTaskDuration();
        int remainingTaskDuration = computationCostSum - completedDuration;

        int endTime = node.getValue();

        for (int i = 0; i < processorCount; i++) {
            trailingTime += endTime - node.getProcessorEndTimes().get(i);
        }

        trailingTime = Math.max(0, trailingTime - remainingTaskDuration);

        return trailingTime;
    }

    /**
     * Generates the starting tasks for a given task graph.
     * @return a Set of the starting tasks.
     */
    private Set<Node> generateStartingTasks() {
        Set<Node> startingTasks = new HashSet<>();

        for (Node taskNode : taskGraph) {
            if (taskNode.getInDegree() == 0) {
                startingTasks.add(taskNode);
            }
        }

        return startingTasks;
    }

    /**
     * @return The most often used Heuristic
     */
    public static Pair<Heuristic, Integer> getMostUsedHeuristic() {
        int maxHeuristicValue = 0;
        Heuristic maxHeuristic = null;

        for (Heuristic heuristic : Heuristic.values()) {
            if (heuristicCount.get(heuristic) > maxHeuristicValue) {
                maxHeuristicValue = heuristicCount.get(heuristic);
                maxHeuristic = heuristic;
            }
        }

        return new Pair<>(maxHeuristic, maxHeuristicValue);
    }

    public static int getIdleTimeUsageCount() {
        if (heuristicCount != null) {
            return heuristicCount.get(Heuristic.IDLE_TIME);
        } else {
            return 0;
        }
    }

    public static int getDataReadyHeuristicCount() {
        if (heuristicCount != null) {
            return heuristicCount.get(Heuristic.DATA_READY);
        } else {
            return 0;
        }
    }

    public static int getBottomLevelHeuristicCount() {
        if (heuristicCount != null) {
            return heuristicCount.get(Heuristic.BOTTOM_LEVEL);
        } else {
            return 0;
        }
    }

}
