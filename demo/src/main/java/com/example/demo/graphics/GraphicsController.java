package com.example.demo.graphics;

import com.example.demo.Launcher;
import com.example.demo.api.EventApi;
import com.example.demo.model.Event;
import com.example.demo.model.Event.EventStatus;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GraphicsController {

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> categoryBarChart;
    @FXML private StackPane subSceneContainer;
    @FXML private Label totalEventsLabel, totalCapacityLabel, upcomingEventsLabel;

    private final EventApi eventApi = new EventApi();

    private SubScene subScene3D;
    private Group root3D;
    private Group barsGroup;
    private Group labelsGroup;

    // Camera + rotation control
    private PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(-30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private double lastMouseX, lastMouseY;

    @FXML
    public void initialize() {
        setup3DScene();
        loadAnalytics();
    }

    private void setup3DScene() {
        root3D = new Group();
        barsGroup = new Group();
        labelsGroup = new Group();

        // Put bars and labels in same root so labels rotate with scene but we will counter-rotate them to face camera
        root3D.getChildren().addAll(barsGroup, labelsGroup);

        subScene3D = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene3D.setFill(Color.web("#efefef")); // slightly off-white inside the subscene

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateZ(-1000);
        camera.setTranslateY(-150);

        subScene3D.setCamera(camera);

        // Lighting
        AmbientLight ambient = new AmbientLight(Color.color(0.35, 0.35, 0.35));
        PointLight key = new PointLight(Color.color(1.0, 0.98, 0.9));
        key.setTranslateZ(-800);
        key.setTranslateX(-200);
        key.setTranslateY(-200);

        root3D.getChildren().addAll(ambient, key);

        // Axes for context
        root3D.getChildren().add(createAxes());

        // apply scene rotation transforms (orbit)
        root3D.getTransforms().addAll(rotateX, rotateY);

        enableMouseControls();

        subSceneContainer.getChildren().clear();
        subSceneContainer.getChildren().add(subScene3D);
    }

    private Node createAxes() {
        Group axes = new Group();

        // X axis (left-right)
        Cylinder x = new Cylinder(2, 800);
        x.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        x.setMaterial(new PhongMaterial(Color.DARKGRAY));
        x.setTranslateY(0);

        // Y axis (up-down)
        Cylinder y = new Cylinder(2, 800);
        y.setMaterial(new PhongMaterial(Color.GRAY));
        y.setTranslateX(-420); // move to left a bit to avoid overlap with bars

        // Z axis (depth)
        Cylinder z = new Cylinder(2, 800);
        z.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        z.setMaterial(new PhongMaterial(Color.DARKSLATEGRAY));
        z.setTranslateY(0);

        axes.getChildren().addAll(x, y, z);
        return axes;
    }

    private void enableMouseControls() {
        // Orbit with mouse drag
        subScene3D.setOnMousePressed((MouseEvent e) -> {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        });

        subScene3D.setOnMouseDragged((MouseEvent e) -> {
            double dx = e.getSceneX() - lastMouseX;
            double dy = e.getSceneY() - lastMouseY;
            rotateY.setAngle(rotateY.getAngle() + dx * 0.4); // yaw
            rotateX.setAngle(clamp(rotateX.getAngle() - dy * 0.4, -80, 80)); // pitch
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();

            // rotate labels to face camera by counter-rotating them on Y axis
            labelsGroup.getChildren().forEach(n -> n.setRotate(-rotateY.getAngle()));
        });

        // Zoom with scroll
        subScene3D.addEventHandler(ScrollEvent.SCROLL, (ScrollEvent e) -> {
            double dz = e.getDeltaY();
            double z = camera.getTranslateZ() + (dz * 0.6);
            camera.setTranslateZ(clamp(z, -4000, -300)); // limits
        });
    }

    private double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }

    @FXML private void onRefresh() { loadAnalytics(); }

    private void loadAnalytics() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Event> events = eventApi.list();

                Map<EventStatus, Long> statusCount =
                        events.stream().collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));

                Map<String, Long> categoryCount =
                        events.stream().collect(Collectors.groupingBy(Event::getCategoryName, Collectors.counting()));

                long totalCapacity = events.stream().mapToInt(Event::getCapacity).sum();
                long upcomingEvents = events.stream().filter(e -> e.getStatus() == EventStatus.UPCOMING).count();

                Platform.runLater(() -> {
                    statusPieChart.getData().clear();
                    statusCount.forEach((s, c) -> statusPieChart.getData().add(new PieChart.Data(s.name(), c)));

                    categoryBarChart.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    categoryCount.forEach((cat, c) -> series.getData().add(new XYChart.Data<>(cat, c)));
                    categoryBarChart.getData().add(series);

                    totalEventsLabel.setText(String.valueOf(events.size()));
                    totalCapacityLabel.setText(String.valueOf(totalCapacity));
                    upcomingEventsLabel.setText(String.valueOf(upcomingEvents));

                    build3DChart(events);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void build3DChart(List<Event> events) {
        barsGroup.getChildren().clear();

        if (events.isEmpty()) return;

        double spacing = 70;
        double barWidth = 40;
        double barDepth = 40;

        double maxCapacity = events.stream().mapToDouble(Event::getCapacity).max().orElse(1);
        double maxDesiredHeight = 380.0;
        double scale = maxDesiredHeight / Math.max(1.0, maxCapacity);

        int index = 0;
        List<Animation> animations = new ArrayList<>();

        for (Event e : events) {
            double capacity = e.getCapacity();
            double targetHeight = Math.max(2, capacity * scale);

            Box bar = new Box(barWidth, 2, barDepth);
            bar.setTranslateX(index * spacing - (events.size() * spacing) / 2.0);
            bar.setTranslateY(-1);
            bar.setTranslateZ(0);

            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(getColor(e.getStatus()));
            mat.setSpecularColor(Color.WHITE);
            mat.setSpecularPower(16);
            bar.setMaterial(mat);

            bar.setUserData(e);

            bar.setOnMouseClicked(evt -> {
                Event clicked = (Event) bar.getUserData();
                System.out.println("Clicked event: " + clicked.getName());
            });

            barsGroup.getChildren().add(bar);

            KeyValue kvHeight = new KeyValue(bar.heightProperty(), targetHeight, Interpolator.EASE_OUT);
            KeyValue kvTranslateY = new KeyValue(bar.translateYProperty(), -targetHeight / 2.0, Interpolator.EASE_OUT);

            KeyFrame kf = new KeyFrame(Duration.millis(800 + (index * 60)), kvHeight, kvTranslateY);
            Timeline tl = new Timeline(kf);

            animations.add(tl);

            index++;
        }

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(animations);
        pt.play();
    }


    private Color getColor(EventStatus s) {
        return switch (s) {
            case ACTIVE -> Color.web("#3cb043");       // pleasant green
            case UPCOMING -> Color.web("#1e90ff");     // dodger blue
            case CANCELLED -> Color.web("#ff4c4c");    // red
            case COMPLETED -> Color.web("#8a8a8a");    // gray
        };
    }
}
