package searchertests;

import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import com.example.project2project2team16.searchers.UniformCostSearcher;
import org.graphstream.graph.Graph;

public class UniformCostSearcherTests extends SearcherTests {
    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        UniformCostSearcher searcher = new UniformCostSearcher(problem);
        searcher.InitialiseSearcher();

        return searcher.Search().GetValue();
    }
}
