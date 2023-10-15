package com.example.project2project2team16;

import com.example.project2project2team16.controllers.MainVisualisationController;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.exceptions.InvalidArgsException;
import com.example.project2project2team16.searchers.AStarSearcher;
import com.example.project2project2team16.utils.AppConfig;
import com.example.project2project2team16.utils.ArgsParser;
import com.example.project2project2team16.utils.DotFileParser;
import com.example.project2project2team16.searchers.SchedulingProblem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisualisationApplication extends Application {
    private static MainVisualisationController mainVisualisationController;
    private static AppConfig appConfig;

    /**
     * Returns the node associated to the input file. The method expects that the file is located in
     * "src/main/resources/fxml".
     *
     * @param fxml The name of the FXML file (without extension).
     * @return The node of the input file.
     * @throws IOException If the file is not found.
     */
    private static Parent loadFxml(final String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(VisualisationApplication.class.getResource("fxml/" + fxml + ".fxml"));
        Parent parent = loader.load();
        mainVisualisationController = loader.getController();
        return parent;
    }

    public static MainVisualisationController getMainVisualisationController() {
        return mainVisualisationController;
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFxml("main-visualisation"));
        stage.setResizable(false);
        stage.setTitle("Parallels - Team 16");
        stage.setScene(scene);
        stage.show();
    }

    public static void startSearch() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            Graph taskGraph = DotFileParser.parseDotFile(appConfig.getInputFilePath());
            SchedulingProblem problem = new SchedulingProblem(taskGraph, appConfig.getNumProcessors());
            AStarSearcher searcher = new AStarSearcher(problem);
            searcher.initialiseSearcher();
            DotFileParser.outputDotFile(searcher.search(), taskGraph, appConfig.getOutputFileName());
            mainVisualisationController.stopTimer();
        });

        executorService.shutdown();
    }

    public static void main(String[] args) {
        appConfig = null;
        try {
            appConfig = ArgsParser.parseArgs(args);
        } catch (InvalidArgsException e) {
            System.exit(1);
        }

        GraphVisualisationHelper.instance().setTaskGraph(DotFileParser.parseDotFile(appConfig.getInputFilePath()));
        GraphVisualisationHelper.instance().setProcessorCount(appConfig.getNumProcessors());
        GraphVisualisationHelper.instance().setGraph(new SingleGraph("Search Graph"));
        if (appConfig.isVisualized()) {
            System.setProperty("org.graphstream.ui", "javafx");
            launch();
        } else {
            startSearch();
        }
    }
}