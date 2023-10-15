package searchertests;

import com.example.project2project2team16.utils.DotFileParser;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class MultithreadSearcherTests {
    public abstract Integer RunSearch(Graph taskGraph, Integer processorNum, Integer ThreadCount);

    @Order(1)
    @Test
    public void Nodes11Processor2MultiCore2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(350, RunSearch(taskGraph,2, 2));
    }

    @Order(2)
    @Test
    public void Nodes11Processor4MultiCore2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(227, RunSearch(taskGraph,4,2));
    }

    @Order(3)
    @Test
    public void Nodes11Processor2MultiCore4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(350, RunSearch(taskGraph,2,4));
    }

    @Order(4)
    @Test
    public void Nodes11Processor4MultiCore4Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(227, RunSearch(taskGraph,4,4));
    }

    @Order(5)
    @Test
    public void Nodes11Processor2MultiCore6Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_11_OutTree.dot");

        assertEquals(350, RunSearch(taskGraph,2,6));
    }

    @Order(6)
    @Test
    public void OutTreeBalanced_Nodes_21_2Proc2Core() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_OutTree-Balanced-MaxBf-3_Nodes_21_CCR_1.05_WeightType_Random.dot");

        assertEquals(71, RunSearch(taskGraph,2, 2));
    }

    @Order(7)
    @Test
    public void OutTreeBalanced_Nodes_21_2Proc4Core() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_OutTree-Balanced-MaxBf-3_Nodes_21_CCR_1.05_WeightType_Random.dot");

        assertEquals(71, RunSearch(taskGraph,2, 4));
    }

}
