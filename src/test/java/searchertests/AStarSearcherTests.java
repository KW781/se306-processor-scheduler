package searchertests;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import com.example.project2project2team16.utils.AppConfig;
import org.graphstream.graph.Graph;

public class AStarSearcherTests extends SearcherTests {
    @Override
    public Integer RunSearch(Graph taskGraph, Integer processorNum) {
        SchedulingProblem problem = new SchedulingProblem(taskGraph, processorNum);
        AStarSearcher searcher = new AStarSearcher(problem);
        searcher.initialiseSearcher();

        AppConfig config = new AppConfig(1);
        VisualisationApplication.setAppConfig(config);

        return searcher.search().getValue();
    }
}
