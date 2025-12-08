package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.UserApi;
import com.example.demo.model.User;
import com.example.demo.security.TokenStore;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for managing Users.
 *
 * Handles displaying users in a table, adding, updating, and deleting users.
 * Also manages form inputs including role, activation, 2FA, and email verification.
 */
public class UserController {

    // ===== TableView & Columns =====
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, User.Role> userRoleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Boolean> twoFaColumn;
    @FXML private TableColumn<User, Boolean> emailVerColumn;
    @FXML private TableColumn<User, String> createdAtColumn;

    // ===== Form Inputs =====
    @FXML private TextField userNameField, userEmailField, firstNameField, lastNameField, phoneField;
    @FXML private PasswordField userPasswordField;
    @FXML private ChoiceBox<User.Role> userRoleChoiceBox;
    @FXML private CheckBox activeCheck, twoFaCheck, emailVerifiedCheck;

    // ===== API =====
    private final UserApi userApi = new UserApi();

    /**
     * Initializes the controller.
     * Sets table columns, configures ChoiceBox, loads users, and sets selection listener.
     */
    @FXML
    public void initialize() {
        // ===== Table Columns =====
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        twoFaColumn.setCellValueFactory(new PropertyValueFactory<>("twoFactorEnabled"));
        emailVerColumn.setCellValueFactory(new PropertyValueFactory<>("emailVerified"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));

        // ===== ChoiceBox Setup =====
        userRoleChoiceBox.getItems().setAll(User.Role.values());
        userRoleChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User.Role role) { return role == null ? "" : role.name(); }
            @Override
            public User.Role fromString(String string) { return User.Role.valueOf(string); }
        });
        userRoleChoiceBox.setValue(User.Role.CUSTOMER);

        // ===== Selection Listener =====
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

        // ===== Load Users =====
        loadUsers();

        // ===== Display boolean as Yes/No =====
        setBooleanColumnFactory(activeColumn);
        setBooleanColumnFactory(twoFaColumn);
        setBooleanColumnFactory(emailVerColumn);
    }

    // ==================== LOAD DATA ====================

    /**
     * Loads users asynchronously and populates the table.
     */
    private void loadUsers() {
        CompletableFuture.runAsync(() -> {
            try {
                List<User> list = userApi.list();
                Platform.runLater(() -> userTable.getItems().setAll(list));
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to load users: " + ex.getMessage()));
            }
        });
    }

    // ==================== CRUD ====================

    /**
     * Adds a new user using form data.
     * Performs validation and runs asynchronously.
     */
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
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to add user: " + ex.getMessage()));
            }
        });
    }

    /**
     * Updates the selected user with form data.
     * Performs validation and runs asynchronously.
     */
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
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to update user: " + ex.getMessage()));
            }
        });
    }

    /**
     * Deletes the selected user.
     * Runs asynchronously and removes the user from the table on success.
     */
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
                });
            } catch (Exception ex) {
                Platform.runLater(() -> AlertUtils.error("Failed to delete user: " + ex.getMessage()));
            }
        });
    }

    // ==================== HELPERS ====================

    /**
     * Clears all form fields and table selection.
     */
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

    /**
     * Trims a string and returns null if empty.
     *
     * @param s input string
     * @return trimmed string or null
     */
    private static String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Configures a boolean column to display "Yes"/"No" instead of true/false.
     *
     * @param col TableColumn to configure
     */
    private void setBooleanColumnFactory(TableColumn<User, Boolean> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item ? "Yes" : "No");
            }
        });
    }

    /** Navigate back to the main dashboard */
    @FXML private void backToMain() { Launcher.go("dashboard.fxml", "Dashboard"); }

    /** Logout and return to login screen */
    @FXML public void onLogout() {
        TokenStore.clear();
        Launcher.go("login.fxml", "Login");
    }
}
