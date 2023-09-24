package Model;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DotFileParser {
    public static Graph parseDotFile(String path) {
        Graph graph = new SingleGraph("MyGraph");
        // Create a DOT file source and associate it with the graph
        FileSource fs = new FileSourceDOT();
        fs.addSink(graph);

        try {
            // Load and parse the DOT file
            fs.readAll(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static Model.Graph manualParse(String path) {
        Map<String, Node> nodes = new HashMap<>();
        Map<String, Edge> edges = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.matches("\\w+\\s*\\[.*\\];")) {
                    // matches nodes
                    String[] parts = line.split("\\[");
                    String id = parts[0].trim();
                    double weight = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                    nodes.put(id, new Node(id, weight));
                } else if (line.matches("\\w+->\\w+\\s*\\[.*\\];")) {
                    // matches edges
                    String[] parts = line.split("->|\\[");
                    String source = parts[0].trim();
                    String target = parts[1].trim();
                    double weight = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
                    edges.put(source + "->" + target, new Edge(source, target, weight));
                }
            }
            return new Model.Graph(nodes, edges);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

