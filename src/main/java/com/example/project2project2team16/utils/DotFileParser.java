package com.example.project2project2team16.utils;

import com.example.project2project2team16.searchers.ScheduleNode;
import javafx.util.Pair;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
            System.err.println("Invalid input file.");
            System.exit(1);
        }
        return graph;
    }

    public static void outputDotFile(ScheduleNode optimalSchedule, Graph graph, String outputFileName) {
        String dotFilePath = outputFileName + ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write("digraph " + outputFileName + " {" + System.lineSeparator());
            for (Map.Entry<String, Pair<Integer, Integer>> entry : optimalSchedule.getVisited().entrySet()) {
                int weight = graph.getNode(entry.getKey()).getAttribute("Weight", Double.class).intValue();
                writer.write("\t" + entry.getKey() + "\t" + " [Weight=" + weight + ",Start=" + (entry.getValue().getValue() - weight)  + ",Processor=" + (entry.getValue().getKey() + 1) + "];" + System.lineSeparator());
            }

            graph.edges().forEach(edge -> {
                try {
                    writer.write("\t" + edge.getSourceNode().getId() + " -> " + edge.getTargetNode().getId() + "\t" + " [Weight=" + edge.getAttribute("Weight",Double.class).intValue() + "];" + System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.write("}");
        } catch (FileNotFoundException e) {
            System.err.println("Invalid output file name.");
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
