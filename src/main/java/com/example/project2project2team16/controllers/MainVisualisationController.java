package com.example.project2project2team16.controllers;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.view.Viewer;

public class MainVisualisationController {
    private Graph scheduleSearchGraph;
    @FXML
    private Pane graphPane;
    @FXML
    private Button autoLayoutButton;
    @FXML
    private Button backButton;
    private FxViewer viewer;

    @FXML
    public void initialize() {
        backButton.managedProperty().bind(backButton.visibleProperty());
        backButton.getStyleClass().clear();
        backButton.getStyleClass().add("svgButton");

        autoLayoutButton.managedProperty().bind(autoLayoutButton.visibleProperty());
        autoLayoutButton.getStyleClass().clear();
        autoLayoutButton.getStyleClass().add("svgButtonActive");
    }

    public void setGraphAndDisplay(Graph graph) {
        scheduleSearchGraph = graph;
        graph.setAttribute("ui.stylesheet", "url('file://src/main/resources/com/example/project2project2team16/css/graph.css')");
        graph.setAttribute("ui.quality");

        viewer = new FxViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
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
                Layout layout = new LinLog();
                layout.setQuality(2.0);
                viewer.enableAutoLayout(layout);
            } else {
                System.out.println("svgButtonActive");
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