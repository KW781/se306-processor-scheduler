package com.example.project2project2team16.controllers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.sun.management.OperatingSystemMXBean;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.GraphMetrics;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.EnumSet;


public class MainVisualisationController {
    @FXML
    private AnchorPane graphPane;
    @FXML
    private HBox graphControls;
    @FXML
    private VBox mainBox;
    @FXML
    private VBox startBox;
    @FXML
    private Text cpuText;
    @FXML
    private Text memoryText;
    @FXML
    private Text timeElapsedText;
    @FXML
    private Text currentShortestTimeText;
    @FXML
    private Text nodeLabel;
    @FXML
    private Text nodePathCost;
    @FXML
    private Button autoLayoutButton;
    @FXML
    private Button startButton;
    @FXML
    private Button pointerButton;
    @FXML
    private Button dragButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Arc memoryArc;
    @FXML
    private Arc cpuArc;
    @FXML
    private FxViewer viewer;
    private Graph scheduleSearchGraph;
    private double timeElapsed = 0;
    private Timeline timeline;
    private Double mouseX;
    private Double mouseY;
    static final String INACTIVE_BUTTON = "svgButton";
    static final String ACTIVE_BUTTON = "svgButtonActive";
    static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);


    @FXML
    public void initialize() {
        // Initialise graph controls
        initialiseGraphControl();

        // Disable application until start has been pressed
        mainBox.setDisable(true);
        startBox.setDisable(false);
        startBox.setVisible(true);

        timeline = new Timeline(new KeyFrame(Duration.seconds(0.001),
                actionEvent -> {
                    timeElapsed += 0.001;
                    timeElapsedText.setText(String.format("%.3fs", timeElapsed));
                    // Display cpu and memory usage
                    if (timeElapsed > 0.01) {
                        cpuArc.setLength((getCPUUsage() / 100) * -360);
                        cpuText.setText(String.valueOf(getCPUUsage()));
                    }
                    memoryText.setText(String.valueOf(getMemoryUsage()));
                    memoryArc.setLength(((double) getMemoryUsage() / 100) * -360);
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

    public void setGraphAndDisplay(Graph graph) {
        scheduleSearchGraph = graph;
        scheduleSearchGraph.setAttribute("ui.stylesheet", "url('com/example/project2project2team16/css/graph.css')");
        scheduleSearchGraph.setAttribute("ui.quality");

        viewer = new FxViewer(scheduleSearchGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer.enableAutoLayout();

        FxViewPanel view = (FxViewPanel) viewer.addView(FxViewer.DEFAULT_VIEW_ID, new FxGraphRenderer());
        view.setPrefWidth(graphPane.getPrefWidth());
        view.setPrefHeight(graphPane.getPrefHeight());
        view.getCamera().resetView();
        view.setCursor(Cursor.HAND);
        view.enableMouseOptions();

        // Set mouse settings
        setNodeClicked(view);
        setUpGraphControls(view);

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

    public void initialiseGraphControl() {
        graphControls.setDisable(false);
        graphControls.setVisible(true);

        pointerButton.managedProperty().bind(pointerButton.visibleProperty());
        pointerButton.getStyleClass().clear();
        pointerButton.getStyleClass().add(ACTIVE_BUTTON);

        dragButton.managedProperty().bind(dragButton.visibleProperty());
        dragButton.getStyleClass().clear();
        dragButton.getStyleClass().add(INACTIVE_BUTTON);

        autoLayoutButton.managedProperty().bind(autoLayoutButton.visibleProperty());
        autoLayoutButton.getStyleClass().clear();
        autoLayoutButton.getStyleClass().add(INACTIVE_BUTTON);
    }

    public void setUpGraphControls(FxViewPanel view) {
        // Center graph controller
        autoLayoutButton.setOnMousePressed((mouseEvent -> {
            if (autoLayoutButton.getStyleClass().get(0).equals(INACTIVE_BUTTON)) {
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add(ACTIVE_BUTTON);
                viewer.enableAutoLayout();
                view.getCamera().resetView();
            }
        }));

        autoLayoutButton.setOnMouseReleased(mouseEvent -> {
            autoLayoutButton.getStyleClass().clear();
            autoLayoutButton.getStyleClass().add(INACTIVE_BUTTON);
        });

        // Pointer controller
        pointerButton.setOnMouseClicked((mouseEvent -> {
            if (pointerButton.getStyleClass().get(0).equals(ACTIVE_BUTTON)) {
                return;
            }

            pointerButton.getStyleClass().clear();
            pointerButton.getStyleClass().add(ACTIVE_BUTTON);
            dragButton.getStyleClass().clear();
            dragButton.getStyleClass().add(INACTIVE_BUTTON);

            setNodeClicked(view);
            view.setOnMouseDragged(dragEvent -> {
            });
            view.setCursor(Cursor.HAND);
        }));

        // Drag controller
        dragButton.setOnMouseClicked((mouseEvent -> {
            if (dragButton.getStyleClass().get(0).equals(ACTIVE_BUTTON)) {
                return;
            }
            // Enable drag button and disable pointer
            dragButton.getStyleClass().clear();
            dragButton.getStyleClass().add(ACTIVE_BUTTON);
            pointerButton.getStyleClass().clear();
            pointerButton.getStyleClass().add(INACTIVE_BUTTON);

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
        }));
    }

    /**
     * This method handles the mouse events on a node
     *
     * @param view the current view of the gui graph
     */
    public void setNodeClicked(FxViewPanel view) {
        view.setOnMousePressed(clickEvent -> {
            GraphicElement node = view.findGraphicElementAt(EnumSet.of(InteractiveElement.NODE), clickEvent.getX(), clickEvent.getY());
            if (node != null) {
                node.setAttribute("ui.style", " stroke-mode: plain; stroke-color: #5A57D8; stroke-width: 2.0; size: 25px;");
                nodeLabel.setText(getNodeId(node.getId()));
                nodePathCost.setText((String) node.getAttribute("ui.pathcost"));
            } else {
                nodeLabel.setText("----");
                nodePathCost.setText("----");
            }
        });

        view.setOnMouseReleased(clickEvent -> {
            GraphicElement node = view.findGraphicElementAt(EnumSet.of(InteractiveElement.NODE), clickEvent.getX(), clickEvent.getY());
            if (node != null) {
                node.setAttribute("ui.style", "stroke-mode: none; size: 15px;");
            }
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

    /**
     * This function calculates the current runtime CPU used
     *
     * @return current CPU used in percentage
     */
    public static double getCPUUsage() {
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(osBean.getSystemCpuLoad() * 100));
    }

    /**
     * This function calculates the current runtime memory used
     *
     * @return current memory used in percentage
     */
    public static int getMemoryUsage() {
        long totalMem = Runtime.getRuntime().totalMemory();
        long memUsed = totalMem - Runtime.getRuntime().freeMemory();
        double memoryUsage = ((double) (memUsed) / totalMem) * 100;
        return (int) Math.round(memoryUsage);
    }

    /**
     * This function returns a formatted node id from a given parameter
     *
     * @param nodeId to be changed
     * @return formatted node id
     */
    public static String getNodeId(String nodeId) {
        return nodeId.substring(nodeId.lastIndexOf("@") + 1).trim();
    }
}