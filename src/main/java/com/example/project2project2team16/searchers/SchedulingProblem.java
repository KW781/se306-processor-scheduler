package com.example.project2project2team16.searchers;

import com.example.project2project2team16.exceptions.PreqrequisiteNotMetException;
import com.example.project2project2team16.searchers.enums.Heuristic;
import javafx.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains all information about the provided scheduling problem
 */
public class SchedulingProblem {

    static Graph taskGraph;
    ScheduleNode startingNode;
    Integer taskCount;
    static Integer processorCount;
    static Integer computationCostSum;

    static Map<Heuristic, Integer> heuristicCount;

    public SchedulingProblem(Graph taskGraph, int processorCount) {
        this.taskGraph = taskGraph;
        this.taskCount = taskGraph.getNodeCount();
        this.processorCount = processorCount;

        // initialise the heuristic count hashmap for all heuristics available
        this.heuristicCount = new HashMap<>();
        for (Heuristic heuristic : Heuristic.values()) {
            this.heuristicCount.put(heuristic, 0);
        }

        Set<Node> startingTasks = GenerateStartingTasks();

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

    public ScheduleNode GetStartNode() {
        return startingNode;
    }

    /**
     * Checks if a ScheduleNode has scheduled all required tasks.
     * @param node ScheduleNode to check
     * @return true if goal, false otherwise
     */
    public boolean IsGoal(ScheduleNode node) {
        return node.IsComplete(taskCount);
    }

    /**
     * Returns a list of all the relevant neighbouring states of the specified ScheduleNode
     * Need to check how the ScheduleNode generates neighbours, as it may require extra work
     * after generation.
     *
     * @param node the ScheduleNode to expand
     * @return list of all relevant neighbouring states
     */
    public List<ScheduleNode> GetNeighbourStates(ScheduleNode node) {
        return node.GenerateNeighbours();
    }

    /**
     * Calculates the F value of the specified ScheduleNode by taking
     * the max of multiple heuristics.
     *
     * @param node The ScheduleNode to calculate for.
     * @return the F value of the ScheduleNode.
     */
    public static Integer CalculateF(ScheduleNode node) {
        // If F value already calculated, no need to recalculate
        if (node.fValue != 0) {
            return node.fValue;
        }

        // DRT heuristic is currently commented out as it is not improving runtimes
        int dataReadyTimeHeuristic = 0;
        int loadBalanceHeuristic = loadBalanceHeuristic(node);
//        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node, taskGraph);
        int bottomLevelHeuristic = bottomLevelHeuristic(node);

        int maxHeuristic = Math.max(Math.max(loadBalanceHeuristic, bottomLevelHeuristic), dataReadyTimeHeuristic);

        // Updating which heuristic is used for visualisation information
        if (maxHeuristic == loadBalanceHeuristic) {
            heuristicCount.replace(Heuristic.IDLE_TIME, heuristicCount.get(Heuristic.IDLE_TIME) + 1);
            node.heuristicUsed = Heuristic.IDLE_TIME;
        } else if (maxHeuristic == dataReadyTimeHeuristic) {
            heuristicCount.replace(Heuristic.DATA_READY, heuristicCount.get(Heuristic.DATA_READY) + 1);
            node.heuristicUsed = Heuristic.DATA_READY;
        } else {
            heuristicCount.replace(Heuristic.BOTTOM_LEVEL, heuristicCount.get(Heuristic.BOTTOM_LEVEL) + 1);
            node.heuristicUsed = Heuristic.BOTTOM_LEVEL;
        }

        // Set and return the calculated F value
        node.fValue = maxHeuristic;
        return maxHeuristic;
    }

    /**
     * Performs dfs to calculate the Critical Computational Path cost of the specified node.
     * @param node Node to calculate for.
     * @param dfsMemo Map of already calculated costs.
     * @return Critical Computational Path cost.
     */
    private static int dfs(Node node, Map<String, Integer> dfsMemo) {
        if (dfsMemo.containsKey(node.getId())) {
            return dfsMemo.get(node.getId());
        }

        int cost = 0;

        // For each child, calculate the maximum computational path cost and store the maximum
        List<Node> nodeChildren = node.leavingEdges().map(Edge::getTargetNode).collect(Collectors.toList());
        for (Node child : nodeChildren) {
            int childCost = dfs(child, dfsMemo);

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
    private static int GetCriticalPath(Node node) {
        Map<String, Integer> dfsMemo = new HashMap<>();

        return dfs(node, dfsMemo) - node.getAttribute("Weight", Double.class).intValue();
    }

    /**
     * Initialise the F value for a ScheduleNode with no scheduled tasks.
     * @param node The ScheduleNode to initialise.
     */
    public static void initialiseF(ScheduleNode node) {
        int bottomLevelHeuristic = 0;
        for (Node task : node.availableTasks) {
            int cp = GetCriticalPath(task) + task.getAttribute("Weight", Double.class).intValue();
            bottomLevelHeuristic = Math.max(bottomLevelHeuristic, cp);
        }

        node.fValue = bottomLevelHeuristic;
        heuristicCount.replace(Heuristic.BOTTOM_LEVEL, heuristicCount.get(Heuristic.BOTTOM_LEVEL) + 1);
        node.heuristicUsed = Heuristic.BOTTOM_LEVEL;
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
        if (node.fValue != 0) {
            return node.fValue;
        }

        if (node.IsComplete(taskGraph.getNodeCount())) {
            return node.GetValue();
        }

        int cost = 0;

        // Checks if the last tasks of each processor have any children tasks.
        boolean noMoreChildren = true;
        int minProcessorEndTime = Integer.MAX_VALUE;
        for (int i = 0; i < processorCount; i++) {
            Node task = node.processorLastTasks.get(i);
            if (task != null && task.getOutDegree() > 0) {
                noMoreChildren = false;
                break;
            }
            minProcessorEndTime = Math.min(minProcessorEndTime, node.processorEndTimes.get(i));
        }

        if (noMoreChildren) {
            // If the last tasks of each processor have no more children, we calculate bottom level as
            // Max(available task's critical path + last end time of task's parent's processor)
            // Otherwise, bottom level calculations would stagnate and become useless
            for (Node task : node.availableTasks) {
                int cp = GetCriticalPath(task) + task.getAttribute("Weight", Double.class).intValue();

                int lastParentEndTime = 0;
                for (Node parent : task.enteringEdges().map(Edge::getSourceNode).collect(Collectors.toList())) {
                    lastParentEndTime = Math.max(lastParentEndTime, node.visited.get(parent.getId()).getValue());
                }

                int earliestStartTime = Math.max(lastParentEndTime, minProcessorEndTime);

                cost = Math.max(cost, cp + earliestStartTime);
            }

            if (node.parent != null) {
                if (node.parent.fValue != 0) {
                    cost = Math.max(node.parent.fValue, cost);
                }
            }

            return cost;
        }

        // If parent is not null, no need to recalculate bottom level for all tasks
        // Only recalculate for the last task added.
        if (node.parent != null) {
            if (node.parent.fValue != 0) {
                int cp = GetCriticalPath(node.lastTask);

                cost = Math.max(node.parent.fValue, cp + node.GetProcessorPathCost(node.lastProcessor));

                return cost;
            }
        }

        // If no parent, we calculate bottom level for the last tasks of each processor,
        // taking the maximum as our cost.
        List<Integer> processorEndTimes = node.processorEndTimes;
        List<Node> processorLastTasks = node.processorLastTasks;
        for (int i = 0; i < node.processorCount; i++) {
            Node n = processorLastTasks.get(i);

            if (n == null) {
                continue;
            }

            int cp = GetCriticalPath(n) + processorEndTimes.get(i);
            cost = Math.max(cost, cp);
        }

        return cost;
    }

    /**
     * Calculates the DRT Heuristic for the specified ScheduleNode.
     * @param node The ScheduleNode to calculate for.
     * @param taskGraph The task graph associated with this ScheduleNode.
     * @return The DRT heuristic.
     */
    private static int dataReadyTimeHeuristic(ScheduleNode node, Graph taskGraph) {
        // get all unvisited nodes from the task graph
        List<Node> freeNodes = taskGraph.nodes().filter(taskNode -> !node.visited.containsKey(taskNode.getId())).collect(Collectors.toList());
        int maxDRTHeuristic = 0;
        int minDRT; // the minimum DRT across all processors

        for (Node currentNode : freeNodes) {
            try {
                minDRT = calculateMaxDRT(currentNode, 0, node.visited);
                for (int i = 1; i < node.processorCount; i++) {
                    minDRT = Math.min(minDRT, calculateMaxDRT(currentNode, i, node.visited));
                }
                maxDRTHeuristic = Math.max(maxDRTHeuristic, minDRT + GetCriticalPath(currentNode));
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
        List<Edge> incomingEdges = taskNode.enteringEdges().collect(Collectors.toList());
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
        return (computationCostSum + node.idleTime + calculateTrailingIdleTimes(node))/processorCount;
    }

    //Makes the idle time heuristic work on its own but increases runtime when paired with bottom level
    private static int calculateTrailingIdleTimes(ScheduleNode node) {
        int trailingTime = 0;
        int completedDuration = node.completedTaskDuration;
        int remainingTaskDuration = computationCostSum - completedDuration;

        int endTime = node.GetValue();

        for (int i = 0; i < processorCount; i++) {
            trailingTime += endTime - node.processorEndTimes.get(i);
        }

        trailingTime = Math.max(0, trailingTime - remainingTaskDuration);

        return trailingTime;
    }

    /**
     * Generates the starting tasks for a given task graph.
     * @return a Set of the starting tasks.
     */
    private Set<Node> GenerateStartingTasks() {
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
        return heuristicCount.get(Heuristic.IDLE_TIME);
    }

    public static int getDataReadyHeuristicCount() {
        return heuristicCount.get(Heuristic.DATA_READY);
    }

    public static int getBottomLevelHeuristicCount() {
        return heuristicCount.get(Heuristic.BOTTOM_LEVEL);
    }
}
