package com.example.project2project2team16.helper;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.controllers.MainVisualisationController;
import com.example.project2project2team16.searchers.ScheduleNode;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.awt.*;
import java.util.Random;

public class GraphVisualisationHelper {
    private static GraphVisualisationHelper instance = null;
    private Graph graph;
    private ScheduleNode currentOptimal;

    private GraphVisualisationHelper() {
    }

    public static GraphVisualisationHelper instance() {
        if (instance == null) {
            instance = new GraphVisualisationHelper();
        }

        return instance;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    public void addEdge(Node source, Node target) {
        if (source != null) {
            graph.addEdge("Edge-" + source + "-" + target, source, target, true).setAttribute("ui.min_length", 50);;
        }
    }

    public void addNode(ScheduleNode scheduleNode, ScheduleNode parent) {
        if (graph == null) {
            return;
        }

        Node node = graph.getNode(scheduleNode.toString());
        if (node != null) {
            return;
        }

        node = graph.addNode(scheduleNode.toString());
        node.setAttribute("scheduleNode", scheduleNode);

        if (parent != null) {
            Node parentNode = graph.getNode(parent.toString());
            if (parentNode != null) {
                addEdge(parentNode, node);
            } else {
                addNode(parent, parent.GetParent());
                parentNode = graph.getNode(parent.toString());
                addEdge(parentNode, node);
            }
        }
    }

    private Color generateRandomColour() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public void setStartNode(ScheduleNode node) {
        Node graphNode = graph.getNode(node.toString());
        graphNode.setAttribute("ui.style", "fill-color: #00FF19;");
    }

    public void updateOptimalNode(ScheduleNode newOptimal) {
        MainVisualisationController mainVisualisationController = VisualisationApplication.getMainVisualisationController();
        if (mainVisualisationController != null) {
            mainVisualisationController.updateShortestTime(newOptimal.GetValue());
        }

        Node graphNode = graph.getNode(newOptimal.toString());
        graphNode.setAttribute("ui.style", "fill-color: #FF0000;");

        if (currentOptimal != null) {
            Node prevOptimalNode = graph.getNode(currentOptimal.toString());
            if (prevOptimalNode != null) {
                prevOptimalNode.setAttribute("ui.style", "fill-color: #D9D9D9;");
            }
        }

        currentOptimal = newOptimal;
    }
}
