package searchertests;

import com.example.project2project2team16.model.DotFileParser;
import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFSSearcherTests {

    private Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);

        return searcher.Search().GetValue();
    }

    @Test
    public void Nodes7Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        assertEquals(28, RunSearch(taskGraph,2));
    }

    @Test
    public void Nodes7Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        assertEquals(22, RunSearch(taskGraph,4));
    }

    @Test
    public void Nodes8Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_8_Random.dot");

        assertEquals(581, RunSearch(taskGraph,2));
    }

    @Test
    public void Nodes8Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_8_Random.dot");

        assertEquals(581, RunSearch(taskGraph,4));
    }

    @Test
    public void Nodes9Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_9_SeriesParallel.dot");

        assertEquals(55, RunSearch(taskGraph,2));
    }

    @Test
    public void Nodes9Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_9_SeriesParallel.dot");

        assertEquals(55, RunSearch(taskGraph,4));
    }

    @Test
    public void Nodes10Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_10_Random.dot");

        assertEquals(50, RunSearch(taskGraph,2));
    }

    @Test
    public void Nodes10Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_10_Random.dot");

        assertEquals(50, RunSearch(taskGraph,4));
    }

    @Test
    public void Nodes11Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(350, RunSearch(taskGraph,2));
    }

    @Test
    public void Nodes11Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(227, RunSearch(taskGraph,4));
    }
}
