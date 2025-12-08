
package com.example.demo.model;

public record ResetPasswordRequest(String emailOrUsername, String token, String newPassword) {}


