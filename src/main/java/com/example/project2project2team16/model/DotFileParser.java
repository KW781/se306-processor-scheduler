package com.example.project2project2team16.model;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

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

}
