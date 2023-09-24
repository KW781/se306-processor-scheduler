package Model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

public class DotFileParser {

    public static void main(String[] args) {
        // Create a new graph
        Graph graph = new SingleGraph("MyGraph");

        // Specify the path to your DOT file
        String dotFilePath = "C:\\Users\\bluey\\OneDrive\\Desktop\\TestDotFile.dot";

        // Create a DOT file source and associate it with the graph
        FileSource fs = new FileSourceDOT();
        fs.addSink(graph);

        try {
            // Load and parse the DOT file
            fs.readAll(dotFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Node node : graph) {
            String nodeId = node.getId();
            double nodeWeight = node.getAttribute("weight");
            System.out.println("Node " + nodeId + " has weight: " + nodeWeight);
        }

        // Iterate through edges and print their weights
        for (Edge edge : graph.getEachEdge()) {
            String edgeId = edge.getId();
            double edgeWeight = edge.getAttribute("weight");
            System.out.println("Edge " + edgeId + " has weight: " + edgeWeight);
        }

        // Print some information about the graph
        System.out.println("Number of nodes: " + graph.getNodeCount());
        System.out.println("Number of edges: " + graph.getEdgeCount());
        // You can now work with the parsed graph as needed
    }
}

