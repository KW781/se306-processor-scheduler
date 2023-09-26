module com.example.project2project2team16 {
    requires javafx.controls;
    requires javafx.fxml;
    requires gs.core;


    opens com.example.project2project2team16 to javafx.fxml;
    exports com.example.project2project2team16;
}