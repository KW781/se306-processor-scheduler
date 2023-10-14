module com.example.project2project2team16 {
    requires javafx.controls;
    requires javafx.fxml;
    requires gs.core;
    requires gs.ui.javafx;
    requires java.management;
    requires jdk.management;
    requires java.desktop;

    opens com.example.project2project2team16 to javafx.fxml;
    exports com.example.project2project2team16;
    exports com.example.project2project2team16.controllers;
    opens com.example.project2project2team16.controllers to javafx.fxml;
}