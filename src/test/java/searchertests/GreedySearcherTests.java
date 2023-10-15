package searchertests;

import com.example.project2project2team16.searchers.GreedySearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;

public class GreedySearcherTests extends SearcherTests {
    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        GreedySearcher searcher = new GreedySearcher(problem);
        searcher.initialiseSearcher();

        return searcher.search().getValue();
    }
}
