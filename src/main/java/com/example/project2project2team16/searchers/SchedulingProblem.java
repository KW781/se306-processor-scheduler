package com.example.project2project2team16.searchers;

import com.example.project2project2team16.exceptions.PreqrequisiteNotMetException;
import javafx.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SchedulingProblem {

    static Graph taskGraph;
    ScheduleNode startingNode;
    Integer taskCount;
    static Integer processorCount;
    static Integer computationCostSum;

    public SchedulingProblem(Graph taskGraph, int processorCount) {
        this.taskGraph = taskGraph;
        this.taskCount = taskGraph.getNodeCount();
        this.processorCount = processorCount;

        Set<Node> startingTasks = GenerateStartingTasks();

        startingNode = new ScheduleNode(processorCount, startingTasks);

        calculateComputationCostSum(taskGraph);
    }

    private void  calculateComputationCostSum(Graph taskGraph) {
        computationCostSum = 0;
        for (Node node : taskGraph) {
            computationCostSum += node.getAttribute("Weight", Double.class).intValue();
        }
    }

    public ScheduleNode GetStartNode() {
        return startingNode;
    }

    public boolean IsGoal(ScheduleNode node) {
        return node.IsComplete(taskCount);
    }

    public List<ScheduleNode> GetNeighbourStates(ScheduleNode node) {
        return node.GenerateNeighbours();
    }

    public static Integer CalculateF(ScheduleNode node) {
        if (node.fValue != 0) {
            return node.fValue;
        }

//        int loadBalanceHeuristic = 0;
        int dataReadyTimeHeuristic = 0;
//        int bottomLevelHeuristic = 0;
        int loadBalanceHeuristic = loadBalanceHeuristic(node);
//        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node, taskGraph);
        int bottomLevelHeuristic = bottomLevelHeuristic(node);

        // dataReadyTime seems to just increase the runtime currently
        int maxHeuristic = Math.max(Math.max(loadBalanceHeuristic, bottomLevelHeuristic), dataReadyTimeHeuristic);


        node.fValue = maxHeuristic;
        return maxHeuristic;
    }

    private static int dfs(Node node) {
        int cost = 0;

        List<Node> nodeChildren = node.leavingEdges().map(Edge::getTargetNode).collect(Collectors.toList());
        for (Node child : nodeChildren) {
            int childCost = dfs(child);

            cost = Math.max(cost, childCost);
        }

        cost += node.getAttribute("Weight", Double.class).intValue();

        return cost;
    }

    private static int GetCriticalPath(Node node) {
        return dfs(node) - node.getAttribute("Weight", Double.class).intValue();
    }

    public static void initialiseF(ScheduleNode node) {
        for (Node task : node.availableTasks) {
            int cp = GetCriticalPath(task) + task.getAttribute("Weight", Double.class).intValue();
            node.fValue = Math.max(node.fValue, cp);
        }
    }

    public static Integer bottomLevelHeuristic(ScheduleNode node) {
        // The F value is defined as f(n) = g(n) + h(n)
        // g(n) is the total cost of the path from the root node to n
        // h(n) is the estimated critical computational path starting from n
        if (node.fValue != 0) {
            return node.fValue;
        }

        if (node.parent != null) {
            if (node.parent.fValue != 0) {
                int cp = GetCriticalPath(node.lastTask);

                node.fValue = Math.max(node.parent.fValue, cp + node.GetProcessorPathCost(node.lastProcessor));

                return node.fValue;
            }
        }

        List<Integer> processorEndTimes = node.processorEndTimes;
        List<Node> processorLastTasks = node.processorLastTasks;
        for (int i = 0; i < node.processorCount; i++) {
            Node n = processorLastTasks.get(i);

            if (n == null) {
                continue;
            }

            int cp = GetCriticalPath(n) + processorEndTimes.get(i);
            node.fValue = Math.max(node.fValue, cp);
        }

        return node.fValue;
    }

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

    private static int calculateMaxDRT(Node taskNode, Integer processor, Map<String, Pair<Integer, Integer>> visited) {
        int maxDRT = 0;
        List<Edge> incomingEdges = taskNode.enteringEdges().collect(Collectors.toList());
        int finishTime;

        for (Edge incomingEdge : incomingEdges) {
            String prereqTaskId = incomingEdge.getSourceNode().getId();
            if (!visited.containsKey(prereqTaskId)) throw new PreqrequisiteNotMetException();
            finishTime = visited.get(prereqTaskId).getValue();
            finishTime += visited.get(prereqTaskId).getKey() == processor ? 0 : incomingEdge.getAttribute("Weight", Double.class).intValue();
            maxDRT = Math.max(maxDRT, finishTime);
        }

        return maxDRT;
    }

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

    private Set<Node> GenerateStartingTasks() {
        Set<Node> startingTasks = new HashSet<>();

        for (Node taskNode : taskGraph) {
            if (taskNode.getInDegree() == 0) {
                startingTasks.add(taskNode);
            }
        }

        return startingTasks;
    }

}
