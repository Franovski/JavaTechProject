package com.example.demo.util;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public final class AlertUtils {
    private AlertUtils() {}


    public static void info(String msg) { show(Alert.AlertType.INFORMATION, "Info", msg); }
    public static void warn(String msg) { show(Alert.AlertType.WARNING, "Warning", msg); }
    public static void error(String msg) { show(Alert.AlertType.ERROR, "Error", msg); }


    private static void show(Alert.AlertType type, String title, String msg) {
        Runnable r = () -> {
            Alert a = new Alert(type, msg, ButtonType.OK);
            a.setHeaderText(null);
            a.setTitle(title);
            a.show();
        };
        if (Platform.isFxApplicationThread()) r.run(); else Platform.runLater(r);
    }
}