package Model;

import java.util.HashMap;
import java.util.Map;

public class Graph {
    Map<String, Node> nodes;
    Map<String, Edge> edges;

    public Graph(Map<String, Node> nodes, Map<String, Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Map<String, Edge> getEdges() {
        return edges;
    }
}
