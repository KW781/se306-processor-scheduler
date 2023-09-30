package searchertests;

import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;

public class AStarSearcherTests extends SearcherTests {
    @Override
    Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        AStarSearcher searcher = new AStarSearcher(problem);

        return searcher.Search().GetValue();
    }
}
