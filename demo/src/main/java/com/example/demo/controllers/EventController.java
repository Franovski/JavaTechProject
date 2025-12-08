package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.CategoryApi;
import com.example.demo.api.EventApi;
import com.example.demo.model.Category;
import com.example.demo.model.Event;
import com.example.demo.model.Event.EventStatus;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for managing Events.
 *
 * Handles displaying events in a table, adding, updating, and deleting events.
 * Also manages event form inputs and Category selection for events.
 */
public class EventController {

    // ===== TableView & Columns =====
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Long> eventIdColumn;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> timeColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TableColumn<Event, String> statusColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, String> categoryNameColumn;

    // ===== Form Inputs =====
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextField capacityField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Category> categoryChoiceBox;
    @FXML private ChoiceBox<EventStatus> statusChoiceBox;

    // ===== APIs & Helpers =====
    private final EventApi eventApi = new EventApi();
    private final CategoryApi categoryApi = new CategoryApi();
    private final Map<Long, String> categoryMap = new HashMap<>();

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Initializes the controller.
     * Sets table columns, loads categories, events, and configures form selection listener.
     */
    @FXML
    public void initialize() {
        // ===== Table Bindings =====
        eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                Optional.ofNullable(c.getValue().getDate()).map(d -> d.format(DATE_FORMAT)).orElse("")));
        timeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                Optional.ofNullable(c.getValue().getTime()).map(t -> t.format(TIME_FORMAT)).orElse("")));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                Optional.ofNullable(c.getValue().getStatus()).map(Enum::name).orElse("")));
        descriptionColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                Optional.ofNullable(c.getValue().getDescription()).orElse("")));
        categoryNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCategory() != null ? c.getValue().getCategory().getName() : ""));

        // ===== Status ChoiceBox =====
        statusChoiceBox.getItems().setAll(EventStatus.values());

        // ===== Category ComboBox =====
        categoryChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category cat) { return cat != null ? cat.getName() : ""; }
            @Override
            public Category fromString(String s) { return null; }
        });

        // ===== Load Categories & Events =====
        loadCategories();

        // ===== Table Selection Listener =====
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, e) -> {
            if (e != null) {
                nameField.setText(Optional.ofNullable(e.getName()).orElse(""));
                datePicker.setValue(e.getDate());
                timeField.setText(e.getTime() != null ? e.getTime().format(TIME_FORMAT) : "");
                locationField.setText(Optional.ofNullable(e.getLocation()).orElse(""));
                capacityField.setText(String.valueOf(e.getCapacity()));
                statusChoiceBox.setValue(e.getStatus());
                descriptionField.setText(Optional.ofNullable(e.getDescription()).orElse(""));
                imageField.setText(Optional.ofNullable(e.getImage()).orElse(""));
                categoryChoiceBox.setValue(e.getCategory());
            }
        });
    }

    /**
     * Loads categories asynchronously and populates the ComboBox.
     * Also stores a category ID → name map for events.
     */
    private void loadCategories() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Category> categories = categoryApi.list();
                synchronized (categoryMap) {
                    categoryMap.clear();
                    categoryMap.putAll(categories.stream()
                            .collect(java.util.stream.Collectors.toMap(Category::getId, Category::getName)));
                }
                Platform.runLater(() -> {
                    categoryChoiceBox.getItems().setAll(categories);
                    loadEvents(); // load events AFTER categories
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.error("Failed to load categories: " + e.getMessage()));
            }
        });
    }

    /**
     * Loads events asynchronously and sets the table items.
     * Updates event categories from the category map.
     */
    private void loadEvents() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Event> events = eventApi.list();
                for (Event e : events) {
                    if (e.getCategory() != null) {
                        e.getCategory().setName(categoryMap.get(e.getCategory().getId()));
                    }
                }
                Platform.runLater(() -> eventTable.getItems().setAll(events));
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to load events: " + ex.getMessage()));
            }
        });
    }

    /**
     * Adds a new event using the form data.
     * Runs asynchronously and updates the table on success.
     */
    @FXML
    private void onAddEvent() {
        Event e = collectEventFromForm();
        if (e == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Event created = eventApi.create(e);
                Platform.runLater(() -> {
                    eventTable.getItems().add(created);
                    clearForm();
                    AlertUtils.info("Event added successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to add event: " + ex.getMessage()));
            }
        });
    }

    /**
     * Updates the selected event with form data.
     * Runs asynchronously and updates the table on success.
     */
    @FXML
    private void onUpdateEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select an event to update.");
            return;
        }

        Event e = collectEventFromForm();
        if (e == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Event updated = eventApi.update(selected.getId(), e);
                Platform.runLater(() -> {
                    int idx = eventTable.getItems().indexOf(selected);
                    if (idx >= 0) eventTable.getItems().set(idx, updated);
                    clearForm();
                    AlertUtils.info("Event updated successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to update event: " + ex.getMessage()));
            }
        });
    }

    /**
     * Deletes the selected event.
     * Runs asynchronously and removes it from the table on success.
     */
    @FXML
    private void onDeleteEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select an event to delete.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                eventApi.delete(selected.getId());
                Platform.runLater(() -> {
                    eventTable.getItems().remove(selected);
                    clearForm();
                    AlertUtils.info("Event deleted successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to delete event: " + ex.getMessage()));
            }
        });
    }

    /**
     * Collects event data from the form fields.
     * Validates required fields before creating the Event object.
     *
     * @return Event object or null if validation fails
     */
    private Event collectEventFromForm() {
        if (nameField.getText().isBlank() || locationField.getText().isBlank() ||
                capacityField.getText().isBlank() || categoryChoiceBox.getValue() == null ||
                datePicker.getValue() == null || timeField.getText().isBlank()) {
            AlertUtils.warn("Please fill in all fields.");
            return null;
        }

        Event e = new Event();
        e.setName(nameField.getText().trim());
        e.setDate(datePicker.getValue());
        e.setTime(LocalTime.parse(timeField.getText().trim(), TIME_FORMAT));
        e.setLocation(locationField.getText().trim());
        e.setCapacity(Integer.parseInt(capacityField.getText().trim()));
        e.setStatus(statusChoiceBox.getValue());
        e.setDescription(Optional.ofNullable(descriptionField.getText()).orElse("").trim());
        e.setImage(Optional.ofNullable(imageField.getText()).orElse("").trim());
        e.setCategory(categoryChoiceBox.getValue());

        return e;
    }

    /**
     * Clears the form fields and selection in the table.
     */
    private void clearForm() {
        nameField.clear();
        datePicker.setValue(null);
        timeField.clear();
        locationField.clear();
        capacityField.clear();
        statusChoiceBox.getSelectionModel().clearSelection();
        descriptionField.clear();
        imageField.clear();
        categoryChoiceBox.setValue(null);
        eventTable.getSelectionModel().clearSelection();
    }

    /** Navigate back to the main dashboard */
    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }

    /** Logout and return to login screen */
    @FXML public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }
}
