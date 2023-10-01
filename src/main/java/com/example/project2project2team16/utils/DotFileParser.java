package com.example.project2project2team16.utils;

import com.example.project2project2team16.searchers.ScheduleNode;
import javafx.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    public static void outputDotFile(ScheduleNode optimalSchedule, Graph graph, String outputFileName) {
        String dotFilePath = outputFileName + ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write("digraph " + outputFileName + " {" + System.lineSeparator());
            for (Map.Entry<String, Pair<Integer, Integer>> entry : optimalSchedule.GetVisited().entrySet()) {
                int weight = graph.getNode(entry.getKey()).getAttribute("Weight", Double.class).intValue();
                writer.write("\t" + entry.getKey() + "\t" + " [Weight=" + weight + ",Start=" + (entry.getValue().getValue() - weight)  + ",Processor=" + (entry.getValue().getKey() + 1) + "];" + System.lineSeparator());
            }
            for (Edge edge : graph.getEachEdge()) {
                writer.write("\t" + edge.getSourceNode().getId() + " -> " + edge.getTargetNode().getId() + "\t" + " [Weight=" + edge.getAttribute("Weight",Double.class).intValue() + "];" + System.lineSeparator());
            }
            writer.write("}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } ;


    }

}
