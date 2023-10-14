package searchertests;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import com.example.project2project2team16.utils.AppConfig;
import org.graphstream.graph.Graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFSSearcherTests extends SearcherTests {

    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);
        searcher.InitialiseSearcher();

        AppConfig config = new AppConfig(1);
        VisualisationApplication.setAppConfig(config);

        return searcher.Search().GetValue();
    }

    public Integer RunSearch(Graph taskGraph, Integer processorNum, Integer threadCount) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        DFSSearcher searcher = new DFSSearcher(problem);
        searcher.InitialiseSearcher();

        AppConfig config = new AppConfig(threadCount);
        VisualisationApplication.setAppConfig(config);

        return searcher.Search().GetValue();
    }
}
