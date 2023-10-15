package searchertests;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.AStarSearcherMultithreaded;
import com.example.project2project2team16.searchers.SchedulingProblem;
import com.example.project2project2team16.utils.AppConfig;
import org.graphstream.graph.Graph;

public class AStarSearcherMultithreadedTests extends MultithreadSearcherTests {
    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum, Integer threadCount) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        AStarSearcherMultithreaded searcher = new AStarSearcherMultithreaded(problem);
        searcher.initialiseSearcher();

        AppConfig config = new AppConfig(threadCount);
        VisualisationApplication.setAppConfig(config);

        return searcher.search().getValue();
    }
}
