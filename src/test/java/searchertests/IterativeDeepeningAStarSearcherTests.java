package searchertests;

import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.IterativeDeepeningAStarSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;

public class IterativeDeepeningAStarSearcherTests extends SearcherTests {
    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        IterativeDeepeningAStarSearcher searcher = new IterativeDeepeningAStarSearcher(problem);

        return searcher.Search().GetValue();
    }
}
