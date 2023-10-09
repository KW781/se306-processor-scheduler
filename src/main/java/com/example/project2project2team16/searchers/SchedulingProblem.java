package com.example.project2project2team16.searchers;

import javafx.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;

public class SchedulingProblem {

    Graph taskGraph;
    ScheduleNode startingNode;
    Integer taskCount;

    public SchedulingProblem(Graph taskGraph, int processorCount) {
        this.taskGraph = taskGraph;
        this.taskCount = taskGraph.getNodeCount();

        Set<Node> startingTasks = GenerateStartingTasks();

        startingNode = new ScheduleNode(processorCount, startingTasks);
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
        int loadBalanceHeuristic = loadBalanceHeuristic(node);
//        int bottomLevelHeuristic = bottomLevelHeuristic(node);
//        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node);

//        int maxHeuristic = Math.max(loadBalanceHeuristic, bottomLevelHeuristic);
//
//        return Math.max(maxHeuristic, dataReadyTimeHeuristic);;
        System.out.println("heuristic: " +  dataReadyTimeHeuristic);
        return dataReadyTimeHeuristic;
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

        Iterable<Edge> parents = taskNode.getEachEnteringEdge();

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
        int computationSum = 0;
        int idleSum = 0;

        // sum all the computation times of all the nodes in the graph
        for (Node task : taskGraph) {
            computationSum += task.getAttribute("Weight", Double.class);
        }

        // sum all the idle times of all the processors
        for (int i = 0; i < node.processorCount; i++) {
            idleSum += calculateIdleTimeForProcessor(node, i);
        }

        return (computationSum + idleSum)/node.processorCount;
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
