package searchertests;

import com.example.project2project2team16.utils.DotFileParser;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.ScheduleNode;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.Test;

public class OutputParseTests {

    private ScheduleNode RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);
        searcher.initialiseSearcher();

        return searcher.search();
    }

    // TODO Maybe don't need to do this?
    @Test
    public void Nodes7Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        DotFileParser.outputDotFile(RunSearch(taskGraph,2), taskGraph, "Nodes_7_OutTree");
    }
}
