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

    Graph taskGraph;
    ScheduleNode startingNode;
    Integer taskCount;
    Integer processorCount;
    Integer computationCostSum;

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

    public Integer getMaximumHeuristic(ScheduleNode node) {
        // int loadBalanceHeuristic = loadBalanceHeuristic(node);
        int loadBalanceHeuristic = 0;

        //int bottomLevelHeuristic = 0;
        int bottomLevelHeuristic = bottomLevelHeuristic(node);
        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node);

        int maxHeuristic = Math.max(Math.max(loadBalanceHeuristic, bottomLevelHeuristic), dataReadyTimeHeuristic);
//
//        return Math.max(maxHeuristic, dataReadyTimeHeuristic);;
        System.out.println("heuristic: " + maxHeuristic);
        node.fValue = maxHeuristic;
        return maxHeuristic;
    }
    private int[] dfs(Node node) {
        // 0 = cost
        // 1 = num of tasks
        int[] result = new int[]{0, 0};

        List<Edge> edges = node.leavingEdges().collect(Collectors.toList());
        for (Edge edge : edges) {
            int[] childResult = dfs(edge.getTargetNode());

            if (childResult[0] > result[0] || (childResult[0] == result[0] && childResult[1] > result[1])) {
                result = childResult;
            }
        }

        result[0] += node.getAttribute("Weight", Double.class).intValue();
        result[1]++;

        return result;
    }

    private int GetCriticalPath(Node node) {
        int cost = 0;

        List<Edge> edges = node.leavingEdges().collect(Collectors.toList());

        for (Edge edge : edges) {
            int[] result = dfs(edge.getTargetNode());

            int freeProcessors = processorCount - result[1];

            int numProcessorsToUse = freeProcessors > 0 ? processorCount - freeProcessors : processorCount;

            if (result[0] / numProcessorsToUse > cost) {
                cost = result[0];
            }
        }

        return cost;
    }

    public Integer bottomLevelHeuristic(ScheduleNode node) {
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

    private int dataReadyTimeHeuristic(ScheduleNode node) {
        // get all unvisited nodes from the task graph
        List<Node> freeNodes = this.taskGraph.nodes().filter(taskNode -> !node.visited.containsKey(taskNode.getId())).collect(Collectors.toList());
        int maxDRTHeuristic = 0;
        int minDRT; // the minimum DRT across all processors

        for (Node currentNode : freeNodes) {
            try {
                minDRT = calculateMaxDRT(currentNode, 0, node.visited);
                for (int i = 1; i < node.processorCount; i++) {
                    minDRT = Math.min(minDRT, calculateMaxDRT(currentNode, i, node.visited));
                }
                maxDRTHeuristic = Math.max(maxDRTHeuristic, minDRT + GetCriticalPath(currentNode));
            } catch (PreqrequisiteNotMetException e) {
            }
        }

        return maxDRTHeuristic;
    }

    private int calculateMaxDRT(Node taskNode, Integer processor, Map<String, Pair<Integer, Integer>> visited) {
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

    private int loadBalanceHeuristic(ScheduleNode node) {
        return (computationCostSum + node.idleTime + calculateTrailingIdleTimes(node))/processorCount;
    }

    private int calculateTrailingIdleTimes(ScheduleNode node) {
        int maxEndTime = node.GetValue();
        int trailingTime = 0;
        for (int i = 0; i < node.processorCount; i++) {
            if (node.processorEndTimes.get(i) != maxEndTime) {
                trailingTime += maxEndTime - node.processorEndTimes.get(i);
            }
        }
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
