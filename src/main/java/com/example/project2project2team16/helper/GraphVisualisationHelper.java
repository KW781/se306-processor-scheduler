package com.example.project2project2team16.helper;

import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.enums.Heuristic;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;

public class GraphVisualisationHelper {
    private static GraphVisualisationHelper instance = null;
    private Graph graph;
    private Graph taskGraph;
    private ScheduleNode currentOptimal;
    private Integer currentScheduleNumber = 0;
    private Integer processorCount;
    private Map<String, ScheduleNode> createdSchedules = new HashMap<>();
    private Map<Integer, String> nodeColours = new HashMap<>();
    static final String LABEL = "ui.label";
    static final String HEURISTIC = "ui.heuristic";
    static final String HEURISTIC_COST = "ui.heuristicCost";
    static final String SCHEDULE = "ui.schedule";
    static final String COLOR = "ui.color";

    private GraphVisualisationHelper() {
    }

    public static GraphVisualisationHelper instance() {
        if (instance == null) {
            instance = new GraphVisualisationHelper();
        }

        return instance;
    }
    public void setTaskGraph(Graph taskGraph) {
        this.taskGraph = taskGraph;
    }
    public Graph getTaskGraph() {
        return taskGraph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setProcessorCount(int numProcessors) {
        this.processorCount = numProcessors;
    }

    public int getProcessorCount() {
        return processorCount;
    }

    public void addEdge(Node source, Node target) {
        if (source != null) {
            graph.addEdge("Edge-" + source + "-" + target, source, target, true).setAttribute("ui.min_length", 50);;
        }
    }
    private Color generateRandomColour() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private String getColour(int threadId) {
        if (nodeColours.containsKey(threadId)) {
            return nodeColours.get(threadId);
        } else {
            Color randomColour = generateRandomColour();
            String colorCode = "rgb(" +
                    randomColour.getRed() + "," +
                    randomColour.getGreen() + "," +
                    randomColour.getBlue() + ")";

            nodeColours.put(threadId, colorCode);
            return colorCode;
        }
    }
    public void addNode(ScheduleNode scheduleNode, ScheduleNode parent) {
        createdSchedules.put(scheduleNode.toString(), scheduleNode);
        if (graph == null) {
            return;
        }

        Node node = graph.getNode(scheduleNode.toString());
        if (node != null) {
            return;
        }

        if (parent != null) {
            Node parentNode = graph.getNode(parent.toString());
            createdSchedules.put(scheduleNode.toString(), scheduleNode);
            if (parentNode != null) {
                currentScheduleNumber++;
                node = graph.addNode(scheduleNode.toString());
                // Set node attributes to be displayed
                node.setAttribute(LABEL, currentScheduleNumber - 1);
                node.setAttribute("ui.style", "fill-color: " + getColour(scheduleNode.getThreadId()) + ";");
                node.setAttribute(HEURISTIC_COST, scheduleNode.getfValue().toString());
                node.setAttribute(HEURISTIC, getHeuristic(scheduleNode));
                node.setAttribute(SCHEDULE, scheduleNode.toString());
                addEdge(parentNode, node);
            } else {
                addNode(parent, parent.getParent());
                currentScheduleNumber++;
                node = graph.addNode(scheduleNode.toString());

                // Set node attributes to be displayed
                node.setAttribute(LABEL, currentScheduleNumber - 1);
                node.setAttribute("ui.style", "fill-color: " + getColour(scheduleNode.getThreadId()) + ";");
                node.setAttribute(HEURISTIC_COST, scheduleNode.getfValue().toString());
                node.setAttribute(HEURISTIC, getHeuristic(scheduleNode));
                node.setAttribute(SCHEDULE, scheduleNode.toString());
                parentNode = graph.getNode(parent.toString());
                addEdge(parentNode, node);
            }
        } else {
            node = graph.addNode(scheduleNode.toString());

            // Set node attributes to be displayed
            node.setAttribute(LABEL, currentScheduleNumber);
            node.setAttribute("ui.style", "fill-color: " + getColour(scheduleNode.getThreadId()) + ";");
            node.setAttribute(HEURISTIC_COST, scheduleNode.getfValue().toString());
            node.setAttribute(HEURISTIC, getHeuristic(scheduleNode));
            currentScheduleNumber++;
        }
    }

    public ScheduleNode getScheduleNode(String schedule) {
        return createdSchedules.get(schedule);
    }

    public void setStartNode(ScheduleNode node) {
        if (graph == null) {
            return;
        }

        Node graphNode = graph.getNode(node.toString());
        graphNode.setAttribute("ui.style", "fill-color: #00FF19;");
    }

    public void updateOptimalNode(ScheduleNode newOptimal) {
        if (graph == null) {
            return;
        }

        Node graphNode = graph.getNode(newOptimal.toString());
        graphNode.setAttribute("ui.prevcolor", graphNode.getAttribute("ui.style"));
        graphNode.setAttribute("ui.style", "fill-color: #FF0000;");

        if (currentOptimal != null) {
            Node prevOptimalNode = graph.getNode(currentOptimal.toString());
            if (prevOptimalNode != null) {
                prevOptimalNode.setAttribute("ui.style", prevOptimalNode.getAttribute("ui.prevcolor"));
            }
        }

        currentOptimal = newOptimal;
    }

    public ScheduleNode getCurrentOptimal() {
        return currentOptimal;
    }

    public String getHeuristic(ScheduleNode node) {
        Heuristic currentHeuristic = node.getHeuristicUsed();

        switch (currentHeuristic) {
            case BOTTOM_LEVEL:
                return "BOTTOM-LVL";
            case IDLE_TIME:
                return "IDLE-TIME";
            case DATA_READY:
                return "DATA_READY";
        }
        return null;
    }
}
