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

        this.pruneDuplicateTasks();

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

    private void pruneDuplicateTasks() {
        // get nodes that have no incoming edges
        List<Node> rootNodes = this.taskGraph.nodes().filter(node -> node.getInDegree() == 0).collect(Collectors.toList());
        Set<Node> visited = new HashSet<>();
        Queue<Node> nodeQueue = new ArrayDeque<>();
        boolean allNodesVisited = false;

        // check for equivalent tasks between root nodes
        for (int i = 0; i < rootNodes.size(); i++) {
            Node outerNode = rootNodes.get(i);
            for (int j = i + 1; j < rootNodes.size(); j++) {
                Node innerNode = rootNodes.get(j);
                if (areTasksEquivalent(outerNode, innerNode)) {
                    // if the tasks are equivalent, add a directed edge between the two with a weight of zero
                    // note that the id assigned to the edge is arbitrary
                    Edge newEdge = this.taskGraph.addEdge(outerNode.getId() + innerNode.getId() + "virtual", outerNode, innerNode, true);
                    newEdge.setAttribute("Weight", 0.0);
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
                                // note that the id assigned to the edge is arbitrary
                                Edge newEdge = this.taskGraph.addEdge(outerChildNode.getId() + innerChildNode.getId() + "virtual", outerChildNode, innerChildNode, true);
                                newEdge.setAttribute("Weight", 0.0);
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
        int loadBalanceHeuristic = loadBalanceHeuristic(node);
//        int loadBalanceHeuristic = 0;

        //int bottomLevelHeuristic = 0;
        int bottomLevelHeuristic = bottomLevelHeuristic(node);
        int dataReadyTimeHeuristic = dataReadyTimeHeuristic(node, taskGraph);

        // dataReadyTime seems to just increase the runtime currently
        int maxHeuristic = Math.max(Math.max(loadBalanceHeuristic, bottomLevelHeuristic), dataReadyTimeHeuristic);


        node.fValue = maxHeuristic;
        return maxHeuristic;
    }

    private static int dfs(Node node) {
        // 0 = cost
        // 1 = num of tasks
//        int[] result = new int[]{0, 0};
//
//        List<Edge> edges = node.leavingEdges().collect(Collectors.toList());
//        for (Edge edge : edges) {
//            int[] childResult = dfs(edge.getTargetNode());
//
//            if (childResult[0] > result[0] || (childResult[0] == result[0] && childResult[1] > result[1])) {
//                result = childResult;
//            }
//        }
//
//        result[0] += node.getAttribute("Weight", Double.class).intValue();
//        result[1]++;
//
//        return result;
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
