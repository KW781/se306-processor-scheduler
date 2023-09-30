package com.example.project2project2team16;

import com.example.project2project2team16.exceptions.InvalidArgsException;
import com.example.project2project2team16.utils.AppConfig;
import com.example.project2project2team16.utils.ArgsParser;
import com.example.project2project2team16.utils.DotFileParser;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.graphstream.graph.Graph;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        AppConfig appConfig = null;
        try {
            appConfig = ArgsParser.parseArgs(args);
        } catch (InvalidArgsException e) {
            System.exit(1);
        }

        Graph taskGraph = DotFileParser.parseDotFile(appConfig.getInputFilePath());
        SchedulingProblem problem = new SchedulingProblem(taskGraph, appConfig.getNumProcessors());
        DFSSearcher searcher = new DFSSearcher(problem);

        System.out.println("Input File: " + appConfig.getInputFilePath());
        System.out.println("Number of Cores: " + appConfig.getNumCores());
        System.out.println("Visualize: " + appConfig.isVisualized());
        System.out.println("Output File: " + appConfig.getOutputFileName());

        DotFileParser.outputDotFile(searcher.Search(), taskGraph, appConfig.getOutputFileName());
        if (appConfig.isVisualized()) launch();
    }
}