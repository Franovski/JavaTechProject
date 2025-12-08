package com.example.demo.model;


// If OTP is required, token will be null/empty.
// If OTP not required, token contains the JWT.
public record AuthResponse(boolean otpRequired, String token) {}
