package com.example.project2project2team16.controllers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;

public class MainVisualisationController {
    @FXML
    private Text timeElapsedText;
    @FXML
    private Text currentShortestTimeText;
    private Graph scheduleSearchGraph;
    @FXML
    private Pane graphPane;
    @FXML
    private Button autoLayoutButton;
    @FXML
    private Button backButton;
    @FXML
    private Button startButton;
    @FXML
    private VBox mainBox;
    @FXML
    private VBox startBox;
    private FxViewer viewer;
    private double timeElapsed = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
        mainBox.setDisable(true);
        startBox.setDisable(false);
        startBox.setVisible(true);

        backButton.managedProperty().bind(backButton.visibleProperty());
        backButton.getStyleClass().clear();
        backButton.getStyleClass().add("svgButton");

        autoLayoutButton.managedProperty().bind(autoLayoutButton.visibleProperty());
        autoLayoutButton.getStyleClass().clear();
        autoLayoutButton.getStyleClass().add("svgButtonActive");

        timeline = new Timeline(new KeyFrame(Duration.seconds(0.01),
                actionEvent -> {
                    timeElapsed += 0.01;
                    timeElapsedText.setText(String.format("%.2fs", timeElapsed));
                }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);

        startButton.setOnMouseClicked(mouseEvent -> {
            startBox.setDisable(true);
            startBox.setVisible(false);
            mainBox.setDisable(false);

            setGraphAndDisplay(GraphVisualisationHelper.getGraph());
            timeline.play();
            VisualisationApplication.startSearch();
        });
    }

    public void stopTimer() {
        timeline.stop();
    }

    public void updateShortestTime(Integer shortestTime) {
        Platform.runLater(() -> {
            currentShortestTimeText.setText(shortestTime.toString());
        });
    }

    public void setGraphAndDisplay(Graph graph) {
        scheduleSearchGraph = graph;
        scheduleSearchGraph.setAttribute("ui.stylesheet", "url('file://src/main/resources/com/example/project2project2team16/css/graph.css')");
        scheduleSearchGraph.setAttribute("ui.quality");

        viewer = new FxViewer(scheduleSearchGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer.enableAutoLayout();

        FxDefaultView view = (FxDefaultView) viewer.addDefaultView(false);
        view.setPrefWidth(graphPane.getPrefWidth());
        view.setPrefHeight(graphPane.getPrefHeight());
        view.getCamera().resetView();

        autoLayoutButton.setOnMouseClicked((mouseEvent -> {
            if (autoLayoutButton.getStyleClass().get(0).equals("svgButton")) {
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add("svgButtonActive");
                viewer.enableAutoLayout();
            } else {
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add("svgButton");
                viewer.disableAutoLayout();
            }
        }));

        graphPane.getChildren().add(view);
    }

    public Graph getScheduleSearchGraph() {
        return scheduleSearchGraph;
    }
}