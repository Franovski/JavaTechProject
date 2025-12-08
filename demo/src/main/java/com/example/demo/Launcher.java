package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;


public final class Launcher {

    private static Stage stage;
    private static Scene scene; // reuse one scene
    private static final String FXML_PREFIX = "/com/example/demo/fxml/";
    private static final String STYLES_PATH = "/com/example/demo/styles.css";
    private static final String ICON_PATH   = "/com/example/demo/icon.png"; // optional

    private Launcher() {}

    public static void init(Stage primary) {
        stage = Objects.requireNonNull(primary, "primary stage is null");

        // Try to set an app icon if present (non-fatal if missing)
        URL iconUrl = HelloApplication.class.getResource(ICON_PATH);
        if (iconUrl != null) {
            try { stage.getIcons().add(new Image(iconUrl.toExternalForm())); } catch (Exception ignored) {}
        }
    }

    public static void go(String fxmlFileName, String title) {
        Runnable task = () -> {
            try {
                ensureInitialized();

                URL url = HelloApplication.class.getResource(FXML_PREFIX + fxmlFileName);
                if (url == null) {
                    throw new IllegalStateException("FXML not found: " + FXML_PREFIX + fxmlFileName);
                }

                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                if (scene == null) {
                    scene = new Scene(root);
                    applyGlobalStyles(scene);
                    stage.setScene(scene);
                } else {
                    scene.setRoot(root); // reuse scene, keep size/position
                }

                if (title != null && !title.isBlank()) {
                    stage.setTitle(title);
                }

                if (!stage.isShowing()) stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setHeaderText("Failed to load FXML");
                alert.setContentText("Error loading " + fxmlFileName + ":\n" + ex.getMessage());
                alert.showAndWait();
                throw new RuntimeException("Failed to load " + fxmlFileName + ": " + ex.getMessage(), ex);
            }

        };

        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }

    private static void ensureInitialized() {
        if (stage == null) {
            throw new IllegalStateException("Launcher.init(stage) must be called before Launcher.go(...)");
        }
    }

    private static void applyGlobalStyles(Scene s) {
        URL css = HelloApplication.class.getResource(STYLES_PATH);
        if (css != null) {
            String uri = css.toExternalForm();
            if (!s.getStylesheets().contains(uri)) {
                s.getStylesheets().add(uri);
            }
        }
    }
}
