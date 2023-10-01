package com.example.project2project2team16.helper;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.ScheduleNode;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.awt.*;
import java.util.Random;

public class GraphVisualisationHelper {
    private static Graph graph;

    public static void setGraph(Graph graph) {
        GraphVisualisationHelper.graph = graph;
    }

    public static Graph getGraph() {
        return graph;
    }

    public static void addEdge(Node source, Node target) {
        if (source != null) {
            graph.addEdge("Edge-" + source + "-" + target, source, target, true).setAttribute("ui.min_length", 50);;
        }
    }

    public static void addNode(ScheduleNode scheduleNode, ScheduleNode parent) {
        Node node = graph.addNode(scheduleNode.toString());
        node.setAttribute("scheduleNode", scheduleNode);

        Color randomColour = generateRandomColour();
        node.setAttribute("ui.style",
            "fill-color: rgb(" +
                    randomColour.getRed() + "," +
                    randomColour.getGreen() + "," +
                    randomColour.getBlue() + ");"
        );

        if (parent != null) {
            Node parentNode = graph.getNode(parent.toString());
            if (parentNode != null) {
                addEdge(parentNode, node);
            }
        }
    }

    private static Color generateRandomColour() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static void setStartNode(ScheduleNode node) {
        Node graphNode = graph.getNode(node.toString());
        graphNode.setAttribute("ui.style", "fill-color: #00FF19;");
    }

    public static void updateOptimalNode(ScheduleNode newOptimal) {
        VisualisationApplication.getMainVisualisationController().updateShortestTime(newOptimal.GetValue());
    }
}
