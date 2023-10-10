package com.example.project2project2team16.searchers;

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
//        int loadBalanceHeuristic = loadBalanceHeuristic(node);
        int bottomLevelHeuristic = bottomLevelHeuristic(node);
//        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node);

//        int maxHeuristic = Math.max(loadBalanceHeuristic, bottomLevelHeuristic);
//
//        return Math.max(maxHeuristic, dataReadyTimeHeuristic);;
        System.out.println("heuristic: " + bottomLevelHeuristic);
        return bottomLevelHeuristic;
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
        Set<Node> freeTasks = node.availableTasks;

        int maxDataReadyTime = Integer.MIN_VALUE;

        for (Node freeTask : freeTasks) {
            int minDataReadyTime = calculateMinDRT(freeTask, node.processorCount, node.visited);
            maxDataReadyTime = Math.max(maxDataReadyTime, minDataReadyTime);
        }
        return maxDataReadyTime;
    }

    private int calculateMinDRT(Node taskNode, Integer processor, Map<String, Pair<Integer, Integer>> visited) {
        int DRT = 0;

        Iterable<Edge> parents = taskNode.enteringEdges().collect(Collectors.toList());

        for (Edge parent : parents) {
            Node sourceTask = parent.getSourceNode();
            int parentProcessor = visited.get(sourceTask.getId()).getKey();
            int communicationCost = parent.getAttribute("Weight", Double.class).intValue();

            int earliestStartTime = 0;

            if (parentProcessor != processor) {
                earliestStartTime = DRT + communicationCost;
            } else {
                earliestStartTime = DRT;
            }

            DRT = Math.max(DRT, earliestStartTime);
        }
        return DRT;
    }

    private int loadBalanceHeuristic(ScheduleNode node) {
        int idleSum = 0;

        // sum all the idle times of all the processors
        for (int i = 0; i < node.processorCount; i++) {
            idleSum += calculateIdleTimeForProcessor(node, i);
        }

        return (computationCostSum + idleSum)/node.processorCount;
    }

    /*
     * Used by the idle time heuristics to find the idle times in a single processor for a given partial schedule
     */
    private int calculateIdleTimeForProcessor(ScheduleNode node, int i) {
        int idleTime = 0;
        int currentTime = 0;

        // looks at each visited task in the partial schedule and calculates the idle time between each task
        // this is so slow right now... can we make it faster?
        //TODO make this way faster
        for (Map.Entry<String, Pair<Integer, Integer>> entry : node.visited.entrySet()) {
            String taskName = entry.getKey();
            int processor = entry.getValue().getKey();
            int endTime = entry.getValue().getValue();

            if (processor == i) {
                int computationTime = taskGraph.getNode(taskName).getAttribute("Weight", Double.class).intValue();
                idleTime += endTime - currentTime - computationTime;
            }
            currentTime = endTime;
        }
        int lastTaskEndTime = node.processorEndTimes.get(i);
        if (lastTaskEndTime > currentTime) {
            idleTime += lastTaskEndTime - currentTime;
        }

        return idleTime;
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
