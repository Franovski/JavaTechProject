package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.UserApi;
import com.example.demo.model.User;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import com.example.demo.util.TableUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for managing Users.
 * Simplified/resilient column sizing to avoid ghost column / alignment issues.
 */
public class UserController {

    // ===== TableView & Columns =====
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> activeColumn;
    @FXML private TableColumn<User, String> twoFaColumn;
    @FXML private TableColumn<User, String> emailVerColumn;
    @FXML private TableColumn<User, String> createdAtColumn;

    // ===== Form Inputs =====
    @FXML private TextField userNameField, userEmailField, firstNameField, lastNameField, phoneField;
    @FXML private PasswordField userPasswordField;
    @FXML private ChoiceBox<User.Role> userRoleChoiceBox;
    @FXML private CheckBox activeCheck, twoFaCheck, emailVerifiedCheck;

    // ===== API =====
    private final UserApi userApi = new UserApi();

    @FXML
    public void initialize() {
        // -------------------------
        // Cell value factories
        // -------------------------
        userIdColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getId())));
        userNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
        userEmailColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        firstNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLastName()));
        phoneColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPhone()));
        userRoleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getRole() != null ? c.getValue().getRole().name() : ""));
        activeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Boolean.TRUE.equals(c.getValue().getIsActive()) ? "Yes" : "No"));
        twoFaColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Boolean.TRUE.equals(c.getValue().getTwoFactorEnabled()) ? "Yes" : "No"));
        emailVerColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(Boolean.TRUE.equals(c.getValue().getEmailVerified()) ? "Yes" : "No"));
        createdAtColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCreatedAtFormatted()));

        // -------------------------
        // Resize policy: constrained
        // -------------------------
        // Use CONSTRAINED_RESIZE_POLICY so JavaFX distributes the remaining space
        // to columns rather than creating a phantom/filler column.
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // -------------------------
        // Prefer/min widths (keeps layout stable)
        // -------------------------
        // These are conservative sizes. Constrained policy will expand columns to fit table width.
        userIdColumn.setPrefWidth(60);
        userIdColumn.setMinWidth(40);

        userNameColumn.setPrefWidth(150);
        userNameColumn.setMinWidth(100);

        userEmailColumn.setPrefWidth(220);
        userEmailColumn.setMinWidth(120);

        firstNameColumn.setPrefWidth(120);
        firstNameColumn.setMinWidth(80);

        lastNameColumn.setPrefWidth(120);
        lastNameColumn.setMinWidth(80);

        phoneColumn.setPrefWidth(130);
        phoneColumn.setMinWidth(90);

        userRoleColumn.setPrefWidth(100);
        userRoleColumn.setMinWidth(80);

        activeColumn.setPrefWidth(70);
        activeColumn.setMinWidth(60);

        twoFaColumn.setPrefWidth(70);
        twoFaColumn.setMinWidth(60);

        emailVerColumn.setPrefWidth(80);
        emailVerColumn.setMinWidth(60);

        createdAtColumn.setPrefWidth(180);
        createdAtColumn.setMinWidth(120);

        // Ensure columns are resizable (so the constrained policy can allocate free space)
        userTable.getColumns().forEach(col -> col.setResizable(true));

        // -------------------------
        // Ellipsis on long text cols (your TableUtils)
        // -------------------------
        userNameColumn.setCellFactory(TableUtils.createEllipsisCell());
        userEmailColumn.setCellFactory(TableUtils.createEllipsisCell());
        firstNameColumn.setCellFactory(TableUtils.createEllipsisCell());
        lastNameColumn.setCellFactory(TableUtils.createEllipsisCell());

        // -------------------------
        // Styling / alignment
        // -------------------------
        TableUtils.style(userTable,
                userIdColumn, userNameColumn, userEmailColumn, firstNameColumn, lastNameColumn,
                phoneColumn, userRoleColumn, activeColumn, twoFaColumn, emailVerColumn, createdAtColumn
        );

        activeColumn.setStyle("-fx-alignment: CENTER;");
        twoFaColumn.setStyle("-fx-alignment: CENTER;");
        emailVerColumn.setStyle("-fx-alignment: CENTER;");
        userRoleColumn.setStyle("-fx-alignment: CENTER;");

        // Fixed row height (keeps visuals consistent)
        userTable.setFixedCellSize(28);

        // -------------------------
        // ChoiceBox Setup
        // -------------------------
        userRoleChoiceBox.getItems().setAll(User.Role.values());
        userRoleChoiceBox.setConverter(new StringConverter<>() {
            @Override public String toString(User.Role role) { return role != null ? role.name() : ""; }
            @Override public User.Role fromString(String string) { return User.Role.valueOf(string); }
        });
        userRoleChoiceBox.setValue(User.Role.CUSTOMER);

        // -------------------------
        // Selection listener
        // -------------------------
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, u) -> {
            if (u != null) {
                userNameField.setText(u.getUsername());
                userEmailField.setText(u.getEmail());
                firstNameField.setText(u.getFirstName());
                lastNameField.setText(u.getLastName());
                phoneField.setText(u.getPhone());
                userRoleChoiceBox.setValue(u.getRole());
                activeCheck.setSelected(Boolean.TRUE.equals(u.getIsActive()));
                twoFaCheck.setSelected(Boolean.TRUE.equals(u.getTwoFactorEnabled()));
                emailVerifiedCheck.setSelected(Boolean.TRUE.equals(u.getEmailVerified()));
                userPasswordField.clear();
            }
        });

        // -------------------------
        // Load data
        // -------------------------
        loadUsers();

        // small defensive refresh after scene is ready to force layout & avoid sticky ghost column
        userTable.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                Platform.runLater(() -> {
                    // refresh triggers re-layout under constrained policy
                    userTable.refresh();
                });
            }
        });
    }

    // ==================== LOAD DATA ====================
    private void loadUsers() {
        CompletableFuture.runAsync(() -> {
            try {
                List<User> list = userApi.list();
                Platform.runLater(() -> {
                    userTable.getItems().setAll(list);
                    // after items loaded we refresh to ensure columns are laid out correctly
                    userTable.refresh();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to load users: " + ex.getMessage()));
            }
        });
    }

    // ==================== CRUD ====================
    @FXML
    private void onAddUser() {
        String username = userNameField.getText().trim();
        String email = userEmailField.getText().trim();
        String password = userPasswordField.getText();
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertUtils.warn("Please fill in username, email and password.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                User u = new User();
                u.setUsername(username);
                u.setEmail(email);
                u.setPassword(password);
                u.setFirstName(trimOrNull(firstNameField.getText()));
                u.setLastName(trimOrNull(lastNameField.getText()));
                u.setPhone(trimOrNull(phoneField.getText()));
                u.setRole(userRoleChoiceBox.getValue());
                u.setIsActive(activeCheck.isSelected());
                u.setTwoFactorEnabled(twoFaCheck.isSelected());
                u.setEmailVerified(emailVerifiedCheck.isSelected());

                User created = userApi.create(u);
                Platform.runLater(() -> {
                    userTable.getItems().add(created);
                    clearForm();
                    AlertUtils.info("User added successfully!");
                    userTable.refresh();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to add user: " + ex.getMessage()));
            }
        });
    }

    @FXML
    private void onUpdateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.warn("Please select a user to update."); return; }

        String username = userNameField.getText().trim();
        String email = userEmailField.getText().trim();
        if (username.isEmpty() || email.isEmpty()) {
            AlertUtils.warn("Username and email cannot be empty.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                User u = new User();
                u.setId(selected.getId());
                u.setUsername(username);
                u.setEmail(email);
                String pw = userPasswordField.getText();
                if (pw != null && !pw.isBlank()) u.setPassword(pw);

                u.setFirstName(trimOrNull(firstNameField.getText()));
                u.setLastName(trimOrNull(lastNameField.getText()));
                u.setPhone(trimOrNull(phoneField.getText()));
                u.setRole(userRoleChoiceBox.getValue());
                u.setIsActive(activeCheck.isSelected());
                u.setTwoFactorEnabled(twoFaCheck.isSelected());
                u.setEmailVerified(emailVerifiedCheck.isSelected());

                User updated = userApi.update(u);
                Platform.runLater(() -> {
                    int idx = userTable.getItems().indexOf(selected);
                    if (idx >= 0) userTable.getItems().set(idx, updated);
                    clearForm();
                    AlertUtils.info("User updated successfully!");
                    userTable.refresh();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to update user: " + ex.getMessage()));
            }
        });
    }

    @FXML
    private void onDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.warn("Please select a user to delete."); return; }

        CompletableFuture.runAsync(() -> {
            try {
                userApi.delete(selected.getId());
                Platform.runLater(() -> {
                    userTable.getItems().remove(selected);
                    clearForm();
                    AlertUtils.info("User deleted successfully!");
                    userTable.refresh();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to delete user: " + ex.getMessage()));
            }
        });
    }

    // ==================== HELPERS ====================
    private void clearForm() {
        userNameField.clear();
        userEmailField.clear();
        userPasswordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        userRoleChoiceBox.setValue(User.Role.CUSTOMER);
        activeCheck.setSelected(true);
        twoFaCheck.setSelected(false);
        emailVerifiedCheck.setSelected(false);
        userTable.getSelectionModel().clearSelection();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }

    @FXML public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }
}
