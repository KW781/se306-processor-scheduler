package Model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

public class DotFileParser {
    public static Graph parseDotFile(String pathName) {
        Graph graph = new SingleGraph("MyGraph");

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
        return graph;
    }

}

