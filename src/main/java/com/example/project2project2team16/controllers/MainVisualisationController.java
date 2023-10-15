package com.example.project2project2team16.controllers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.SchedulingProblem;
import com.sun.management.OperatingSystemMXBean;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.GraphMetrics;
import org.graphstream.ui.view.util.InteractiveElement;
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
    private Text nodeLabel;
    @FXML
    private Text nodePathCost;
    @FXML
    private Text nodeWeight;
    @FXML
    private Text idlePercText;
    @FXML
    private Text dataPercText;
    @FXML
    private Text bottomPercText;
    @FXML
    private Text statusText;
    @FXML
    private Button autoLayoutButton;
    @FXML
    private Button startButton;
    @FXML
    private Button pointerButton;
    @FXML
    private Button dragButton;
    @FXML
    private Arc memoryArc;
    @FXML
    private Arc cpuArc;
    @FXML
    private PieChart heuristicPieChart;
    @FXML
    private Rectangle statusBar;
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
    private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // Initialise graph controls
        initialiseGraphControl();

        // Disable application until start has been pressed
        mainBox.setDisable(true);
        startBox.setDisable(false);
        startBox.setVisible(true);

        createPieChart();

        timeline = new Timeline(new KeyFrame(Duration.seconds(0.001),
                actionEvent -> {
                    timeElapsed += 0.001;
                    Platform.runLater(() -> {
                        timeElapsedText.setText(String.format("%.3fs", timeElapsed));
                        // Display cpu and memory usage
                        if (timeElapsed > 0.01) {
                            cpuArc.setLength((getCPUUsage() / 100) * -360);
                            cpuText.setText(String.valueOf(getCPUUsage()));
                        }
                        memoryText.setText(String.valueOf(getMemoryUsage()));
                        memoryArc.setLength(((double) getMemoryUsage() / 100) * -360);
                        updatePieChart(SchedulingProblem.getIdleTimeUsageCount(), SchedulingProblem.getDataReadyHeuristicCount(), SchedulingProblem.getBottomLevelHeuristicCount());
                    });
                }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);

        startButton.setOnMouseClicked(mouseEvent -> {
            startBox.setDisable(true);
            startBox.setVisible(false);
            mainBox.setDisable(false);

            statusText.setText("RUNNING");
            statusBar.setStyle("-fx-fill: #00ff00; -fx-opacity: 20%");
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
        autoLayoutButton.setOnMouseClicked((mouseEvent -> {
            if (autoLayoutButton.getStyleClass().get(0).equals(INACTIVE_BUTTON)) {
                viewer.enableAutoLayout();
                view.getCamera().resetView();
                autoLayoutButton.getStyleClass().clear();
                autoLayoutButton.getStyleClass().add(INACTIVE_BUTTON);
            }
        }));

        autoLayoutButton.setOnMousePressed((mouseEvent -> {
            autoLayoutButton.getStyleClass().clear();
            autoLayoutButton.getStyleClass().add(ACTIVE_BUTTON);
        }));

        autoLayoutButton.setOnMouseReleased(mouseEvent -> {
            autoLayoutButton.getStyleClass().clear();
            autoLayoutButton.getStyleClass().add(INACTIVE_BUTTON);
            viewer.disableAutoLayout();
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
                nodeLabel.setText((String) node.getAttribute("ui.heuristic"));
                nodePathCost.setText((String) node.getAttribute("ui.heuristicCost"));
                nodeWeight.setText(node.getLabel());
            } else {
                nodeLabel.setText("-");
                nodePathCost.setText("-");
                nodeWeight.setText("");
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
        statusText.setText("COMPLETED");
        statusBar.setStyle("-fx-fill: #FF0000; -fx-opacity: 20%");
    }

    private void createPieChart() {
        pieChartData.add(new PieChart.Data("Idle-Time Count", 1));
        pieChartData.add(new PieChart.Data("Data-Ready Count", 1));
        pieChartData.add(new PieChart.Data("Bottom-Level Count", 1));
        heuristicPieChart.setLabelsVisible(false);
        heuristicPieChart.setLegendVisible(false);
        heuristicPieChart.setData(pieChartData);
    }

    private void updatePieChart(int idleTimeUsageCount, int dataReadyHeuristicCount, int bottomLevelHeuristicCount) {
        double idlePerc, dataPerc, bottomPerc = 0;
        int total = idleTimeUsageCount + dataReadyHeuristicCount + bottomLevelHeuristicCount;
        DecimalFormat df = new DecimalFormat("#.#");

        pieChartData.get(0).setPieValue(idleTimeUsageCount);
        pieChartData.get(1).setPieValue(dataReadyHeuristicCount);
        pieChartData.get(2).setPieValue(bottomLevelHeuristicCount);

        // Calculate percentages
        idlePerc = Double.parseDouble(df.format((((double) (idleTimeUsageCount) / total) * 100)));
        idlePercText.setText(idlePerc + "%");
        dataPerc =  Double.parseDouble(df.format((((double) (dataReadyHeuristicCount) / total) * 100)));
        dataPercText.setText(dataPerc + "%");
        bottomPerc = Double.parseDouble(df.format((((double) (bottomLevelHeuristicCount) / total) * 100)));
        bottomPercText.setText(bottomPerc + "%");
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
}