module com.example.demo {
    // JavaFX
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    // HTTP + Jackson
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // Other
    requires java.prefs;
    requires java.desktop;

    // Reflective access for FXML
    opens com.example.demo.controllers to javafx.fxml;
    opens com.example.demo.graphics to javafx.fxml;
    opens com.example.demo.model to com.fasterxml.jackson.databind, javafx.base;

    // Export your public API if needed
    exports com.example.demo;
}
