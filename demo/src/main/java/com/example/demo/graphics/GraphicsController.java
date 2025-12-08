package com.example.demo.graphics;

import com.example.demo.Launcher;
import com.example.demo.api.EventApi;
import com.example.demo.model.Event;
import com.example.demo.model.Event.EventStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GraphicsController {

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> categoryBarChart;
    @FXML private StackPane subSceneContainer;
    @FXML private Label totalEventsLabel, totalCapacityLabel, upcomingEventsLabel;

    private final EventApi eventApi = new EventApi();

    // 3D Scene
    private SubScene subScene3D;
    private Group root3D;

    @FXML
    public void initialize() {
        setup3DScene();
        loadAnalytics();
    }

    private void setup3DScene() {
        root3D = new Group();
        subScene3D = new SubScene(root3D, 600, 600, true, null);
        subScene3D.setFill(Color.web("#f4f4f4"));
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-800);
        camera.setTranslateY(-200);
        camera.setTranslateX(0);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        subScene3D.setCamera(camera);
        subSceneContainer.getChildren().add(subScene3D);
    }

    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }
    
    @FXML
    private void onRefresh() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Event> events = eventApi.list();

                Map<EventStatus, Long> statusCount = events.stream()
                        .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));

                Map<String, Long> categoryCount = events.stream()
                        .collect(Collectors.groupingBy(Event::getCategoryName, Collectors.counting()));

                long totalCapacity = events.stream().mapToInt(Event::getCapacity).sum();
                long upcomingEvents = events.stream().filter(e -> e.getStatus() == EventStatus.UPCOMING).count();

                Platform.runLater(() -> {
                    statusPieChart.getData().clear();
                    statusCount.forEach((status, count) -> statusPieChart.getData().add(new PieChart.Data(status.name(), count)));

                    categoryBarChart.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    categoryCount.forEach((cat, count) -> series.getData().add(new XYChart.Data<>(cat, count)));
                    categoryBarChart.getData().add(series);

                    totalEventsLabel.setText(String.valueOf(events.size()));
                    totalCapacityLabel.setText(String.valueOf(totalCapacity));
                    upcomingEventsLabel.setText(String.valueOf(upcomingEvents));

                    build3DChart(events);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void build3DChart(List<Event> events) {
        root3D.getChildren().clear();

        int index = 0;
        double spacing = 50;
        double width = 30;

        for (Event e : events) {
            double height = e.getCapacity();
            Box box = new Box(width, height, width);

            PhongMaterial material = new PhongMaterial();
            switch (e.getStatus()) {
                case ACTIVE -> material.setDiffuseColor(Color.GREEN);
                case UPCOMING -> material.setDiffuseColor(Color.BLUE);
                case CANCELLED -> material.setDiffuseColor(Color.RED);
                case COMPLETED -> material.setDiffuseColor(Color.GRAY);
            }
            box.setMaterial(material);

            box.setTranslateX(index * spacing - (events.size() * spacing) / 2.0);
            box.setTranslateY(-height / 2.0);
            box.setTranslateZ(0);

            root3D.getChildren().add(box);
            index++;
        }
    }
}
