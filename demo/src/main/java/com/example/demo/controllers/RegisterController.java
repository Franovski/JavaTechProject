package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.AuthApi;
import com.example.demo.model.RegisterRequest;
import com.example.demo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField username;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField phone;

    @FXML
    private void onRegister() {
        try {
            String u  = safe(username);
            String e  = safe(email);
            String p  = safe(password);
            String cp = safe(confirmPassword);
            String f  = safe(firstName);
            String l  = safe(lastName);
            String ph = safe(phone);

            // Basic validation
            if (u.isEmpty() || e.isEmpty() || p.isEmpty() || cp.isEmpty()) {
                AlertUtils.warn("Please fill in username, email, password, and confirm password.");
                return;
            }

            if (!p.equals(cp)) {
                AlertUtils.warn("Passwords do not match.");
                return;
            }

            // Always use default role CUSTOMER
            RegisterRequest req = new RegisterRequest();
            req.setUsername(u);
            req.setEmail(e);
            req.setPassword(p);
            req.setFirstName(emptyToNull(f));
            req.setLastName(emptyToNull(l));
            req.setPhone(emptyToNull(ph));
            req.setRole("CUSTOMER");

            AuthApi.register(req);

            AlertUtils.info("Account created successfully! Please login.");
            Launcher.go("login.fxml", "Login");

        } catch (Exception ex) {
            AlertUtils.warn("Registration failed: " + ex.getMessage());
        }
    }

    @FXML
    private void goLogin() {
        Launcher.go("login.fxml", "Login");
    }

    // Helpers
    private static String safe(TextField t) {
        return (t == null || t.getText() == null) ? "" : t.getText().trim();
    }

    private static String safe(PasswordField t) {
        return (t == null || t.getText() == null) ? "" : t.getText().trim();
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
