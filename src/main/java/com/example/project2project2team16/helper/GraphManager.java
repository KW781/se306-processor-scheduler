package com.example.project2project2team16.helper;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.ScheduleNode;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphManager {
    private static Graph graph;

    public static void setGraphAndDisplay(Graph graph) {
        GraphManager.graph = graph;
        VisualisationApplication.getMainVisualisationController().setGraphAndDisplay(graph);
    }

    public static Edge addEdge(Node source, Node target) {
        return graph.addEdge("Edge-" + source + "-" + target, source, target, true);
    }

    public static Node addNode(ScheduleNode scheduleNode) {
        Node node = graph.addNode(scheduleNode.toString());
        node.setAttribute("scheduleNode", scheduleNode);

        return node;
    }

    public static void setStartNode(Node node) {
        node.setAttribute("ui.style", "fill-color: #00FF19;");
    }

    public static void setEndNode(Node node) {
        node.setAttribute("ui.style", "fill-color: #FF0000;");
    }
}
