package com.example.project2project2team16;

import com.example.project2project2team16.controllers.MainVisualisationController;
import com.example.project2project2team16.helper.SceneManager;
import com.example.project2project2team16.model.DotFileParser;
import com.example.project2project2team16.searchers.DFSSearcher;
import com.example.project2project2team16.searchers.SchedulingProblem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.graphstream.graph.Graph;

import java.io.IOException;

public class VisualisationApplication extends Application {
    private static MainVisualisationController mainVisualisationController;

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

        switch (fxml) {
            case "main-visualisation":
                mainVisualisationController = loader.getController();
                break;
        }

        return parent;
    }

    public static MainVisualisationController getMainVisualisationController() {
        return mainVisualisationController;
    }

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.addUi(SceneManager.AppScene.MAIN_VISUALISATION, loadFxml("main-visualisation"));

        Scene scene = new Scene(SceneManager.getUiRoot(SceneManager.AppScene.MAIN_VISUALISATION));

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        Graph taskGraph = DotFileParser.parseDotFile("src/test/resources/Nodes_7_OutTree.dot");
        SchedulingProblem problem = new SchedulingProblem(taskGraph, 1);
        DFSSearcher searcher = new DFSSearcher(problem);
        System.out.println(searcher.Search().GetValue());
    }

    public static void main(String[] args) {
//        System.out.println(args[0]); // placeholder to make sure cmd line argument is read
//        Graph taskGraph = DotFileParser.parseDotFile(args[0]);

        System.setProperty("org.graphstream.ui", "javafx");
        System.setProperty("gs.ui.layout", "LinLog");
        launch();
    }
}