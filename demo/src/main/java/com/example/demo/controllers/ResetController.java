package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.AuthApi;
import com.example.demo.security.TempAuth;
import com.example.demo.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ResetController {
    @FXML private TextField email;
    @FXML private TextField code;
    @FXML private PasswordField newPassword;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (TempAuth.resetEmail != null && !TempAuth.resetEmail.isBlank()) {
                email.setText(TempAuth.resetEmail);
            }
        });
    }

    public void onReset() {
        try {
            String e = safe(email.getText());
            String c = safe(code.getText());
            String p = safe(newPassword.getText());

            if (e.isEmpty() || c.isEmpty() || p.isEmpty()) {
                AlertUtils.warn("Enter email, the code you received, and a new password.");
                return;
            }

            AuthApi.resetWithCode(e, c, p);
            AlertUtils.info("Password reset. Please login with your new password.");
            Launcher.go("login.fxml", "Login");
        } catch (Exception ex) {
            AlertUtils.error("Reset failed: " + ex.getMessage());
        }
    }

    private static String safe(String s){ return s == null ? "" : s.trim(); }

    public void goLogin(){ Launcher.go("login.fxml", "Login"); }
}
