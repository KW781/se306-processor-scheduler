package searchertests;

import com.example.project2project2team16.utils.DotFileParser;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class SearcherTests {
    public abstract Integer RunSearch(Graph taskGraph, Integer processorNum);

    @Order(1)
    @Test
    public void Nodes7Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        assertEquals(28, RunSearch(taskGraph,2));
    }

    @Order(2)
    @Test
    public void Nodes7Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");

        assertEquals(22, RunSearch(taskGraph,4));
    }

    @Order(3)
    @Test
    public void Nodes8Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_8_Random.dot");

        assertEquals(581, RunSearch(taskGraph,2));
    }

    @Order(4)
    @Test
    public void Nodes8Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_8_Random.dot");

        assertEquals(581, RunSearch(taskGraph,4));
    }

    @Order(5)
    @Test
    public void Nodes9Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_9_SeriesParallel.dot");

        assertEquals(55, RunSearch(taskGraph,2));
    }

    @Order(6)
    @Test
    public void Nodes9Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_9_SeriesParallel.dot");

        assertEquals(55, RunSearch(taskGraph,4));
    }

    @Order(7)
    @Test
    public void Nodes10Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_10_Random.dot");

        assertEquals(50, RunSearch(taskGraph,2));
    }

    @Order(8)
    @Test
    public void Nodes10Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_10_Random.dot");

        assertEquals(50, RunSearch(taskGraph,4));
    }

    @Order(9)
    @Test
    public void Nodes11Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(350, RunSearch(taskGraph,2));
    }

    @Order(10)
    @Test
    public void Nodes11Processor4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(227, RunSearch(taskGraph,4));
    }
}
