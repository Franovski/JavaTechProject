package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.CategoryApi;
import com.example.demo.model.Category;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller responsible for managing all UI operations related to Categories.
 *
 * Handles loading, creating, updating, and deleting categories using the
 * CategoryApi service. It also controls the TableView, form inputs, and
 * navigation buttons in the Categories view.
 */
public class CategoryController {

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Long>   categoryIdColumn;
    @FXML private TableColumn<Category, String> categoryNameColumn;
    @FXML private TextField categoryNameField;

    private final CategoryApi categoryApi = new CategoryApi();

    // ====================== INITIALIZATION ======================

    /**
     * Initializes the controller by setting column bindings, loading categories,
     * and configuring selection listeners to update the UI form.
     */
    @FXML
    public void initialize() {
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        loadCategories();

        // Update form when selecting a category
        categoryTable.getSelectionModel().selectedItemProperty().addListener((o, oldSel, selected) -> {
            if (selected != null) categoryNameField.setText(selected.getName());
        });
    }

    // ====================== LOAD CATEGORIES ======================

    /**
     * Loads all categories asynchronously from the backend API
     * and populates the table.
     */
    private void loadCategories() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Category> list = categoryApi.list();
                Platform.runLater(() -> categoryTable.getItems().setAll(list));
            } catch (Exception ex) {
                Platform.runLater(() ->
                        AlertUtils.error("Failed to load categories: " + ex.getMessage())
                );
            }
        });
    }

    // ====================== CREATE CATEGORY ======================

    /**
     * Triggered when the user clicks the "Add" button.
     *
     * Validates the name field, creates a new category via API,
     * and updates the table on success.
     */
    @FXML
    private void onAddCategory() {
        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtils.warn("Please enter a category name.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Category created = categoryApi.create(name);
                Platform.runLater(() -> {
                    categoryTable.getItems().add(created);
                    clearForm();
                    AlertUtils.info("Category added successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        AlertUtils.error("Failed to add category: " + ex.getMessage())
                );
            }
        });
    }

    // ====================== UPDATE CATEGORY ======================

    /**
     * Triggered when the user clicks the "Update" button.
     *
     * Ensures a category is selected, applies updates via API,
     * and refreshes the table with modified data.
     */
    @FXML
    private void onUpdateCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select a category to update.");
            return;
        }

        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtils.warn("Category name cannot be empty.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Category updated = categoryApi.update(selected.getId(), name);
                Platform.runLater(() -> {
                    int index = categoryTable.getItems().indexOf(selected);
                    if (index >= 0) categoryTable.getItems().set(index, updated);
                    clearForm();
                    AlertUtils.info("Category updated successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        AlertUtils.error("Failed to update category: " + ex.getMessage())
                );
            }
        });
    }

    // ====================== DELETE CATEGORY ======================

    /**
     * Triggered when the user clicks the "Delete" button.
     *
     * Deletes the selected category from the backend and removes it
     * from the table upon confirmation.
     */
    @FXML
    private void onDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Please select a category to delete.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                categoryApi.delete(selected.getId());
                Platform.runLater(() -> {
                    categoryTable.getItems().remove(selected);
                    clearForm();
                    AlertUtils.info("Category deleted successfully!");
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        AlertUtils.error("Failed to delete category: " + ex.getMessage())
                );
            }
        });
    }

    // ====================== UTILITIES ======================

    /**
     * Clears the form fields and resets the table selection.
     */
    private void clearForm() {
        categoryNameField.clear();
        categoryTable.getSelectionModel().clearSelection();
    }

    // ====================== NAVIGATION ======================

    /**
     * Returns to the main dashboard screen.
     */
    @FXML
    private void backToMain() {
        Launcher.go("dashboard.fxml", "Dashboard");
    }

    /**
     * Logs the user out and redirects to the login screen.
     */
    @FXML
    public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }
}
