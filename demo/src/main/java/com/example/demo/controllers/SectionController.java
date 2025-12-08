package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.EventApi;
import com.example.demo.api.SectionApi;
import com.example.demo.model.Event;
import com.example.demo.model.Section;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for managing Sections.
 *
 * Handles displaying sections in a table, adding, updating, and deleting sections.
 * Also manages form inputs including associated Event selection and Section status.
 */
public class SectionController {

    // ===== TableView & Columns =====
    @FXML private TableView<Section> sectionTable;
    @FXML private TableColumn<Section, Long> sectionIdColumn;
    @FXML private TableColumn<Section, String> sectionNameColumn;
    @FXML private TableColumn<Section, String> statusColumn;
    @FXML private TableColumn<Section, String> eventNameColumn;
    @FXML private TableColumn<Section, Integer> rowCountColumn;
    @FXML private TableColumn<Section, Integer> seatCountColumn;

    // ===== Form Inputs =====
    @FXML private TextField sectionNameField;
    @FXML private ComboBox<Event> eventComboBox;
    @FXML private TextField rowCountField;
    @FXML private TextField seatCountField;
    @FXML private ChoiceBox<Section.SectionStatus> statusChoiceBox;

    // ===== APIs & Helpers =====
    private final SectionApi sectionApi = new SectionApi();
    private final EventApi eventApi = new EventApi();
    private final Map<Long, Event> eventMap = new HashMap<>();

    /**
     * Initializes the controller.
     * Sets table columns, loads events and sections, and configures form selection listener.
     */
    @FXML
    public void initialize() {
        // ===== Table Columns =====
        sectionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sectionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        rowCountColumn.setCellValueFactory(new PropertyValueFactory<>("rowCount"));
        seatCountColumn.setCellValueFactory(new PropertyValueFactory<>("seatCount"));

        statusColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getStatus() != null ? c.getValue().getStatus().name() : "")
        );

        // Display event name from either nested Event or flat field
        eventNameColumn.setCellValueFactory(c -> {
            Section s = c.getValue();
            String name = "";
            if (s.getEvent() != null && s.getEvent().getName() != null) {
                name = s.getEvent().getName();
            } else if (s.getEventName() != null) {
                name = s.getEventName();
            }
            return new ReadOnlyStringWrapper(name);
        });

        // ===== ComboBox / ChoiceBox =====
        statusChoiceBox.getItems().setAll(Section.SectionStatus.values());
        eventComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Event event) { return event != null ? event.getName() : ""; }
            @Override
            public Event fromString(String s) { return null; }
        });

        // ===== Load data =====
        loadEvents();
        loadSections();

        // ===== Selection listener (auto-fill fields) =====
        sectionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, s) -> {
            if (s != null) {
                sectionNameField.setText(s.getName());
                rowCountField.setText(String.valueOf(s.getRowCount()));
                seatCountField.setText(String.valueOf(s.getSeatCount()));
                statusChoiceBox.setValue(s.getStatus());

                Event sectionEvent = s.getEvent();
                if (sectionEvent == null && s.getEventId() != null) {
                    sectionEvent = eventComboBox.getItems().stream()
                            .filter(e -> e.getId().equals(s.getEventId()))
                            .findFirst()
                            .orElse(null);
                }

                eventComboBox.setValue(sectionEvent);
            }
        });
    }

    // ==================== LOAD DATA ====================

    /**
     * Loads all events asynchronously and populates the event ComboBox.
     * Also fills the eventMap for quick lookup by ID.
     */
    private void loadEvents() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Event> events = eventApi.list();
                for (Event e : events) eventMap.put(e.getId(), e);
                Platform.runLater(() -> {
                    eventComboBox.getItems().setAll(events);
                    loadSections(); // after loading events
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.error("Failed to load events: " + e.getMessage()));
            }
        });
    }

    /**
     * Loads all sections asynchronously and populates the table.
     * Ensures that each section has its associated Event object set.
     */
    private void loadSections() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Section> sections = sectionApi.list();

                for (Section s : sections) {
                    if (s.getEvent() == null && s.getEventId() != null) {
                        s.setEvent(eventMap.get(s.getEventId()));
                    }
                    if (s.getEventName() == null && s.getEvent() != null) {
                        s.setEventName(s.getEvent().getName());
                    }
                }

                Platform.runLater(() -> sectionTable.getItems().setAll(sections));
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to load sections: " + ex.getMessage()));
            }
        });
    }

    // ==================== CRUD ====================

    /**
     * Adds a new section using the form data.
     * Runs asynchronously and updates the table on success.
     */
    @FXML
    private void onAddSection() {
        Section s = collectSectionFromForm();
        if (s == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Section created = sectionApi.create(s);
                Platform.runLater(() -> {
                    sectionTable.getItems().add(created);
                    clearForm();
                    AlertUtils.info("Section added successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to add section: " + ex.getMessage()));
            }
        });
    }

    /**
     * Updates the selected section with form data.
     * Runs asynchronously and updates the table on success.
     */
    @FXML
    private void onUpdateSection() {
        Section selected = sectionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select a section to update.");
            return;
        }

        Section s = collectSectionFromForm();
        if (s == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Section updated = sectionApi.update(selected.getId(), s);
                Platform.runLater(() -> {
                    int idx = sectionTable.getItems().indexOf(selected);
                    if (idx >= 0) sectionTable.getItems().set(idx, updated);
                    clearForm();
                    AlertUtils.info("Section updated successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to update section: " + ex.getMessage()));
            }
        });
    }

    /**
     * Deletes the selected section.
     * Runs asynchronously and removes it from the table on success.
     */
    @FXML
    private void onDeleteSection() {
        Section selected = sectionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select a section to delete.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                sectionApi.delete(selected.getId());
                Platform.runLater(() -> {
                    sectionTable.getItems().remove(selected);
                    clearForm();
                    AlertUtils.info("Section deleted successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to delete section: " + ex.getMessage()));
            }
        });
    }

    // ==================== HELPERS ====================

    /**
     * Collects section data from the form fields.
     * Validates required fields before creating the Section object.
     *
     * @return Section object or null if validation fails
     */
    private Section collectSectionFromForm() {
        String name = sectionNameField.getText().trim();
        Event event = eventComboBox.getValue();
        Section.SectionStatus status = statusChoiceBox.getValue();

        if (name.isEmpty() || event == null || status == null ||
                rowCountField.getText().isBlank() || seatCountField.getText().isBlank()) {
            AlertUtils.warn("Please fill in all fields.");
            return null;
        }

        Section s = new Section();
        s.setName(name);
        s.setEvent(event);
        s.setEventId(event.getId());
        s.setEventName(event.getName());
        s.setStatus(status);
        s.setRowCount(Integer.parseInt(rowCountField.getText().trim()));
        s.setSeatCount(Integer.parseInt(seatCountField.getText().trim()));
        return s;
    }

    /**
     * Clears the form fields and selection in the table.
     */
    private void clearForm() {
        sectionNameField.clear();
        eventComboBox.setValue(null);
        rowCountField.clear();
        seatCountField.clear();
        statusChoiceBox.getSelectionModel().clearSelection();
        sectionTable.getSelectionModel().clearSelection();
    }

    /** Navigate back to the main dashboard */
    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }

    /** Logout and return to login screen */
    @FXML public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }
}
