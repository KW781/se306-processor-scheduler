package com.example.project2project2team16.helper;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.ScheduleNode;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphVisualisationHelper {
    private static Graph graph;

    public static Graph getGraph() {
        return graph;
    }

    public static void setGraphAndDisplay(Graph graph) {
        GraphVisualisationHelper.graph = graph;

        if (VisualisationApplication.getMainVisualisationController() != null) {
            VisualisationApplication.getMainVisualisationController().setGraphAndDisplay(graph);
        }
    }

    public static Edge addEdge(Node source, Node target) {
        return graph.addEdge("Edge-" + source + "-" + target, source, target, true);
    }

    public static Node addNode(ScheduleNode scheduleNode, Node parent) {
        Node node = graph.addNode(scheduleNode.toString());
        node.setAttribute("scheduleNode", scheduleNode);

        if (parent != null) {
            addEdge(parent, node);
        }

        return node;
    }

    public static void setStartNode(ScheduleNode node) {
        Node graphNode = graph.getNode(node.toString());
        graphNode.setAttribute("ui.style", "fill-color: #00FF19;");
    }

    public static void setEndNode(ScheduleNode node) {
        Node graphNode = graph.getNode(node.toString());
        graphNode.setAttribute("ui.style", "fill-color: #FF0000;");
    }
}
