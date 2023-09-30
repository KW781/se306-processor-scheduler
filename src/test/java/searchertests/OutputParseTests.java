package searchertests;

import com.example.project2project2team16.model.DotFileParser;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.SchedulingProblem;
import javafx.util.Pair;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OutputParseTests {

    private ScheduleNode RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);

        return searcher.Search();
    }

    // TODO Maybe don't need to do this?
    @Test
    public void Nodes7Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        DotFileParser.outputDotFile(RunSearch(taskGraph,2), taskGraph, "Nodes_7_OutTree");
    }
}
