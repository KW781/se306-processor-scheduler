package searchertests;

import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.utils.DotFileParser;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class SearcherTests {
    public abstract Integer RunSearch(Graph taskGraph, Integer processorNum);

    @BeforeEach
    public void setup() {
        GraphVisualisationHelper.instance().setGraph(new SingleGraph("Search Graph"));
    }

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

    @Order(11)
    @Test
    public void Fork_Join_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Fork_Join_Nodes_10_CCR_0.10_WeightType_Random.dot");

        assertEquals(499, RunSearch(taskGraph,2));
    }

    @Order(12)
    @Test
    public void Independent_Nodes_21_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Independent_Nodes_21_WeightType_Random.dot");

        assertEquals(66, RunSearch(taskGraph,2));
    }
    @Order(13)
    @Test
    public void InTree_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_InTree-Balanced-MaxBf-3_Nodes_10_CCR_0.10_WeightType_Random.dot");

        assertEquals(222, RunSearch(taskGraph,2));
    }
    @Order(14)
    @Test
    public void Fork_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Fork_Nodes_10_CCR_0.10_WeightType_Random.dot");

        assertEquals(300, RunSearch(taskGraph,2));
    }
    @Order(15)
    @Test
    public void InTreeUnBalanced_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_InTree-Unbalanced-MaxBf-3_Nodes_10_CCR_0.10_WeightType_Random.dot");

        assertEquals(344, RunSearch(taskGraph,2));
    }
    @Order(16)
    @Test
    public void Pipeline_Nodes_21_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Pipeline_Nodes_21_CCR_0.10_WeightType_Random.dot");

        assertEquals(904, RunSearch(taskGraph,2));
    }
    @Order(17)
    @Test
    public void Join_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Join_Nodes_10_CCR_0.10_WeightType_Random.dot");

        assertEquals(292, RunSearch(taskGraph,2));
    }


    @Order(18)
    @Test
    public void OutTreeBalanced_Nodes_21_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_OutTree-Balanced-MaxBf-3_Nodes_21_CCR_1.05_WeightType_Random.dot");

        assertEquals(71, RunSearch(taskGraph,2));
    }


    @Disabled
    @Order(19)
    @Test
    public void OutTreeUnbalanced_Nodes_21_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_OutTree-Unbalanced-MaxBf-3_Nodes_21_CCR_0.99_WeightType_Random.dot");

        assertEquals(72, RunSearch(taskGraph,2));
    }
    @Order(20)
    @Test
    public void Random_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Random_Nodes_10_Density_4.50_CCR_10.00_WeightType_Random.dot");

        assertEquals(66, RunSearch(taskGraph,2));
    }
    @Order(21)
    @Test
    public void SeriesParallel_Nodes_10_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_SeriesParallel-MaxBf-5_Nodes_10_CCR_9.97_WeightType_Random.dot");

        assertEquals(59, RunSearch(taskGraph,2));
    }

    @Order(22)
    @Test
    public void Stencil_Nodes_21_2Proc() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/2p_Stencil_Nodes_21_CCR_10.03_WeightType_Random.dot");

        assertEquals(134, RunSearch(taskGraph,2));
    }

    @Order(23)
    @Test
    public void Nodes5ForkOrderProcessor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_5_Fork_Order.dot");

        assertEquals(12, RunSearch(taskGraph,2));
    }

    @Order(24)
    @Test
    public void Nodes5JoinOrderProcessor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_5_Join_Order.dot");

        assertEquals(12, RunSearch(taskGraph,2));
    }

    @Order(25)
    @Test
    public void Nodes5ForkNoProcessor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_5_No_Order.dot");

        assertEquals(12, RunSearch(taskGraph,2));
    }

    @Order(26)
    @Test
    public void Nodes15Edges10Processor3Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/15Nodes10Edges.dot");

        assertEquals(197, RunSearch(taskGraph,3));
    }

    @Order(27)
    @Test
    public void Nodes15Edges80Processor2Test() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/15Nodes80Edges.dot");

        assertEquals(516, RunSearch(taskGraph,2));
    }

    @Order(28)
    @Test
    public void Equivalent_tasks_fork_16() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Equivalent_tasks_fork_16.dot");

        assertEquals(610, RunSearch(taskGraph,2));
    }
    @Order(29)
    @Test
    public void Equivalent_tasks_join_16() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Equivalent_tasks_join_16.dot");

        assertEquals(610, RunSearch(taskGraph,2));
    }
    @Order(30)
    @Test
    public void Equivalent_tasks_fork_join_17() {
        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Equivalent_tasks_fork_join_17.dot");

        assertEquals(680, RunSearch(taskGraph,2));
    }

}
