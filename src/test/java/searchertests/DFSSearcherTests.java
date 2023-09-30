package searchertests;

import com.example.project2project2team16.model.DotFileParser;
import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFSSearcherTests extends SearcherTests {

    @Override
    Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);

        return searcher.Search().GetValue();
    }
}
