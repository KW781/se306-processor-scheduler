package com.example.project2project2team16.controllers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.fx_viewer.util.FxMouseOverMouseManager;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.GraphMetrics;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

import javax.sound.midi.SysexMessage;
import java.util.EnumSet;

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
    private HBox graphControls;
    @FXML
    private HBox scheduleControls;
    @FXML
    private Button backButton;
    @FXML
    private Button startButton;
    @FXML
    private Button pointerButton;
    @FXML
    private Button dragButton;
    @FXML
    private VBox mainBox;
    @FXML
    private VBox startBox;
    private FxViewer viewer;
    private double timeElapsed = 0;
    private Timeline timeline;
    private Double mouseX;
    private Double mouseY;
    final String buttonStyle = "svgButton";
    final String buttonEvent = "svgButtonActive";

    @FXML
    public void initialize() {
        graphControls.setDisable(false);
        graphControls.setVisible(true);

        mainBox.setDisable(true);

        startBox.setDisable(false);
        startBox.setVisible(true);

        pointerButton.managedProperty().bind(pointerButton.visibleProperty());
        pointerButton.getStyleClass().clear();
        pointerButton.getStyleClass().add(buttonEvent);

        dragButton.managedProperty().bind(dragButton.visibleProperty());
        dragButton.getStyleClass().clear();
        dragButton.getStyleClass().add(buttonStyle);

        autoLayoutButton.managedProperty().bind(autoLayoutButton.visibleProperty());
        autoLayoutButton.getStyleClass().clear();
        autoLayoutButton.getStyleClass().add(buttonEvent);

        timeline = new Timeline(new KeyFrame(Duration.seconds(0.001),
                actionEvent -> {
                    timeElapsed += 0.001;
                    timeElapsedText.setText(String.format("%.3fs", timeElapsed));
                }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);

        startButton.setOnMouseClicked(mouseEvent -> {
            startBox.setDisable(true);
            startBox.setVisible(false);
            mainBox.setDisable(false);

            setGraphAndDisplay(GraphVisualisationHelper.instance().getGraph());
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

        FxViewPanel view = (FxViewPanel) viewer.addView(FxViewer.DEFAULT_VIEW_ID, new FxGraphRenderer());
        view.setPrefWidth(graphPane.getPrefWidth());
        view.setPrefHeight(graphPane.getPrefHeight());
        view.getCamera().resetView();

        autoLayoutButton.setOnMouseClicked((mouseEvent -> {
            if (autoLayoutButton.getStyleClass().get(0).equals(buttonStyle)) {
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add(buttonEvent);
                viewer.enableAutoLayout();
                view.getCamera().resetView();
            } else {
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add(buttonStyle);
                viewer.disableAutoLayout();
            }
        }));

        pointerButton.setOnMouseClicked((mouseEvent -> {
            if (pointerButton.getStyleClass().get(0).equals(buttonEvent)) {
                return;
            }

            pointerButton.getStyleClass().clear();
            pointerButton.getStyleClass().add(buttonEvent);

            dragButton.getStyleClass().clear();
            dragButton.getStyleClass().add(buttonStyle);

            view.setOnMousePressed(pressEvent -> {});
            view.setOnMouseDragged(dragEvent -> {});

            view.setCursor(Cursor.DEFAULT);

            view.setMouseManager(new FxMouseManager());
        }));

        dragButton.setOnMouseClicked((mouseEvent -> {
            if (dragButton.getStyleClass().get(0).equals(buttonEvent)) {
                return;
            }

            dragButton.getStyleClass().clear();
            dragButton.getStyleClass().add(buttonEvent);

            pointerButton.getStyleClass().clear();
            pointerButton.getStyleClass().add(buttonStyle);


            view.setOnMousePressed(pressEvent -> {
                mouseX = pressEvent.getX();
                mouseY = pressEvent.getY();
            });

            view.setOnMouseDragged(dragEvent -> {
                GraphMetrics metrics = view.getCamera().getMetrics();

                double deltaX = metrics.lengthToGu((dragEvent.getX() - mouseX) * 1, StyleConstants.Units.PX);
                double deltaY = metrics.lengthToGu((dragEvent.getY() - mouseY) * 1, StyleConstants.Units.PX);

                Point3 point3 = view.getCamera().getViewCenter();

                view.getCamera().setViewCenter(point3.x - deltaX, point3.y + deltaY, 0);

                mouseX = dragEvent.getX();
                mouseY = dragEvent.getY();
            });

            view.setCursor(Cursor.MOVE);

            view.setMouseManager(new MouseManager() {
                @Override
                public void init(GraphicGraph graphicGraph, View view) {

                }
                @Override
                public void release() {

                }
                @Override
                public EnumSet<InteractiveElement> getManagedTypes() {
                    return null;
                }
            });
        }));

        graphPane.getChildren().addAll(view);

        graphPane.setOnScroll(scrollEvent -> {
            if (scrollEvent.getDeltaY() < 0) {
                view.getCamera().setViewPercent(view.getCamera().getViewPercent() + 0.1);
            } else {
                if (view.getCamera().getViewPercent() <= 0.2) {
                    return;
                }

                view.getCamera().setViewPercent(view.getCamera().getViewPercent() - 0.1);
            }
        });
    }
}