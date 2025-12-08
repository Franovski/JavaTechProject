package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.*;
import com.example.demo.model.*;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controller for the main Dashboard view.
 *
 * Handles displaying Events, Sections, Categories, and Users tables,
 * as well as dashboard stats (counts).
 * Also handles navigation buttons and logout functionality.
 */
public class DashboardController {

    // ===== UI Elements =====
    @FXML private Label welcome;

    // ===== Event Table =====
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> eventDateColumn;
    @FXML private TableColumn<Event, String> eventTimeColumn;
    @FXML private TableColumn<Event, String> eventLocationColumn;
    @FXML private TableColumn<Event, Number> eventCapacityColumn;
    @FXML private TableColumn<Event, String> eventStatusColumn;
    @FXML private TableColumn<Event, String> eventCategoryColumn;

    // ===== Section Table =====
    @FXML private TableView<Section> sectionTable;
    @FXML private TableColumn<Section, String> sectionNameColumn;
    @FXML private TableColumn<Section, String> sectionStatusColumn;
    @FXML private TableColumn<Section, String> sectionEventColumn;
    @FXML private TableColumn<Section, Number> sectionRowCountColumn;
    @FXML private TableColumn<Section, Number> sectionSeatCountColumn;

    // ===== Category Table =====
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> categoryNameColumn;

    // ===== User Table =====
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;

    // ===== Dashboard Stats Labels =====
    @FXML private Label eventCountLabel;
    @FXML private Label sectionCountLabel;
    @FXML private Label categoryCountLabel;
    @FXML private Label userCountLabel;

    /**
     * Initializes the controller.
     * Sets welcome message and loads all tables and counts.
     */
    @FXML
    public void initialize() {
        if (welcome == null) {
            System.err.println("[DashboardController] Missing fx:id bindings in dashboard.fxml!");
            return;
        }

        welcome.setText("Welcome 👋");

        Platform.runLater(() -> {
            try {
                if (!TokenStore.hasToken()) {
                    AlertUtils.warn("No token present. Please login again.");
                    Launcher.go("login.fxml", "Login");
                    return;
                }

                MeResponse me = AuthApi.me();
                if (me == null) {
                    AlertUtils.error("Failed to retrieve profile data.");
                    return;
                }

                String full = (me.getFirstName() == null ? "" : me.getFirstName());
                if (me.getLastName() != null && !me.getLastName().isBlank())
                    full += (full.isBlank() ? "" : " ") + me.getLastName();

                if (full.isBlank()) full = me.getUsername();

                welcome.setText("Welcome 👋 " + full);

                loadAllTables();

            } catch (Exception ex) {
                AlertUtils.error("Failed to load profile: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    // ===== Load all tables =====

    /** Load all tables and dashboard counts */
    private void loadAllTables() {
        loadEvents();
        loadSections();
        loadCategories();
        loadUsers();
        loadCounts();
    }

    /**
     * Adjusts table height dynamically based on number of rows.
     *
     * @param table TableView to adjust
     */
    private void setTableHeight(TableView<?> table) {
        if (table.getItems() == null || table.getItems().isEmpty()) {
            table.setPrefHeight(50);
        } else {
            double height = table.getItems().size() * 28 + 32; // 28px per row + header
            table.setPrefHeight(height);
        }
    }

    // ===== Events =====

    /** Loads Event table and sets columns */
    private void loadEvents() {
        try {
            EventApi api = new EventApi();
            List<Event> events = api.list();
            ObservableList<Event> list = FXCollections.observableArrayList(events);

            eventNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
            eventDateColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDateFormatted()));
            eventTimeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTimeFormatted()));
            eventLocationColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLocation()));
            eventCapacityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCapacity()));
            eventStatusColumn.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(c.getValue().getStatus() != null ? c.getValue().getStatus().toString() : "")
            );
            eventCategoryColumn.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(c.getValue().getCategoryName())
            );

            eventTable.setItems(list);
            eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            setTableHeight(eventTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Sections =====

    /** Loads Section table and sets columns */
    private void loadSections() {
        CompletableFuture.runAsync(() -> {
            try {
                SectionApi sectionApi = new SectionApi();
                EventApi eventApi = new EventApi();

                List<Section> sections = sectionApi.list();
                List<Event> events = eventApi.list();

                Map<Long, Event> eventMap = events.stream()
                        .collect(Collectors.toMap(Event::getId, e -> e));

                for (Section s : sections) {
                    if (s.getEvent() == null && s.getEventId() != null) {
                        s.setEvent(eventMap.get(s.getEventId()));
                    }
                }

                ObservableList<Section> list = FXCollections.observableArrayList(sections);

                Platform.runLater(() -> {
                    sectionNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
                    sectionStatusColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                            c.getValue().getStatus() != null ? c.getValue().getStatus().name() : "")
                    );
                    sectionEventColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                            c.getValue().getEventName()
                    ));
                    sectionRowCountColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getRowCount()));
                    sectionSeatCountColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSeatCount()));

                    sectionTable.setItems(list);
                    sectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    setTableHeight(sectionTable);
                });

            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.error("Failed to load sections: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    // ===== Categories =====

    /** Loads Category table and sets columns */
    private void loadCategories() {
        try {
            CategoryApi api = new CategoryApi();
            List<Category> categories = api.list();
            ObservableList<Category> list = FXCollections.observableArrayList(categories);

            categoryNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));

            categoryTable.setItems(list);
            categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            setTableHeight(categoryTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Users =====

    /** Loads User table and sets columns */
    private void loadUsers() {
        try {
            UserApi api = new UserApi();
            List<User> users = api.list();
            ObservableList<User> list = FXCollections.observableArrayList(users);

            firstNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFirstName()));
            lastNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLastName()));
            usernameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
            emailColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
            phoneColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPhone()));
            roleColumn.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(
                            c.getValue().getRole() != null
                                    ? c.getValue().getRole().name().charAt(0) + c.getValue().getRole().name().substring(1).toLowerCase()
                                    : ""
                    )
            );

            userTable.setItems(list);
            userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            setTableHeight(userTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Counts =====

    /** Loads total counts for Events, Sections, Categories, and Users */
    private void loadCounts() {
        try {
            EventApi eventApi = new EventApi();
            SectionApi sectionApi = new SectionApi();
            CategoryApi categoryApi = new CategoryApi();
            UserApi userApi = new UserApi();

            eventCountLabel.setText(String.valueOf(eventApi.list().size()));
            sectionCountLabel.setText(String.valueOf(sectionApi.list().size()));
            categoryCountLabel.setText(String.valueOf(categoryApi.list().size()));
            userCountLabel.setText(String.valueOf(userApi.list().size()));
        } catch (Exception e) {
            e.printStackTrace();
            eventCountLabel.setText("—");
            sectionCountLabel.setText("—");
            categoryCountLabel.setText("—");
            userCountLabel.setText("—");
        }
    }

    // ===== Navigation & Actions =====

    @FXML public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }

    @FXML private void onAddCategoryClicked() { Launcher.go("category.fxml", "Category Dashboard"); }
    @FXML private void onAddUserClicked() { Launcher.go("user.fxml", "User Dashboard"); }
    @FXML private void onAddSectionClicked() { Launcher.go("section.fxml", "Section Dashboard"); }
    @FXML private void onAddEventClicked() { Launcher.go("event.fxml", "Event Dashboard"); }
    @FXML private void onRefreshDashboard() { loadAllTables(); }
    @FXML private void onViewEventAnalytics() { Launcher.go("graphics.fxml", "Event Analytics"); }
}
