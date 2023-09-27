package com.example.project2project2team16;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainVisualisationController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}