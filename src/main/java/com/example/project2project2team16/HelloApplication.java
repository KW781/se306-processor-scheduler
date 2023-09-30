package com.example.project2project2team16;

import com.example.project2project2team16.model.DotFileParser;
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

        String inputFilePath = null;
        int numProcessors = 1;
        try {
            inputFilePath = args[0];
            numProcessors = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Invalid arguments. Please provide a valid input file and number of processors.");
            System.exit(1);
        }

        int numCores = 1; // Default number of cores

        boolean visualize = false;
        String outputFileName = "INPUT-output.dot"; // Default output file name

        // Parse command-line arguments
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("-p") && i + 1 < args.length) {
                try {
                    numCores = Integer.parseInt(args[i + 1]);
                    i++; // Skip the next argument since it's been used
                } catch (NumberFormatException e) {
                    System.err.println("Invalid value for -p. Please provide an integer.");
                    System.exit(1);
                }
            } else if (args[i].equals("-v")) {
                visualize = true;
            } else if (args[i].equals("-o") && i + 1 < args.length) {
                outputFileName = args[i + 1];
                i++; // Skip the next argument since it's been used
            }
        }

        // Check if the input file exists
        if (inputFilePath == null) {
            System.err.println("Input file not provided. Please specify a valid input file.");
            System.exit(1);
        }

        Graph taskGraph = DotFileParser.parseDotFile(inputFilePath);
        SchedulingProblem problem = new SchedulingProblem(taskGraph, numProcessors);
        DFSSearcher searcher = new DFSSearcher(problem);

        System.out.println("Input File: " + inputFilePath);
        System.out.println("Number of Cores: " + numCores);
        System.out.println("Visualize: " + visualize);
        System.out.println("Output File: " + outputFileName);

        DotFileParser.outputDotFile(searcher.Search(), taskGraph, outputFileName);
        launch();
    }
}