package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.AuthApi;
import com.example.demo.security.TempAuth;
import com.example.demo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ForgotController {
    @FXML private TextField emailOrUsername;

    public void onSend(){
        try {
            String v = emailOrUsername.getText()==null? "": emailOrUsername.getText().trim();
            if (v.isEmpty()) { AlertUtils.warn("Enter your email or username."); return; }

            AuthApi.requestResetOtp(v);
            // If user typed an email, remember it to prefill reset screen
            if (v.contains("@")) TempAuth.resetEmail = v;

            AlertUtils.info("If the account exists, a reset code has been sent.");
            Launcher.go("reset.fxml", "Reset Password");
        } catch (Exception ex) {
            AlertUtils.error("Failed to send reset link: " + ex.getMessage());
        }
    }

    public void goLogin(){ Launcher.go("login.fxml", "Login"); }
}
