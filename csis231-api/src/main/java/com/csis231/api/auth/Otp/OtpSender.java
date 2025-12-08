package com.csis231.api.auth.Otp;

import com.csis231.api.user.User;

import java.time.Instant;
public interface OtpSender {
    void send(User user, String purpose, String code, Instant expiresAt);
}
