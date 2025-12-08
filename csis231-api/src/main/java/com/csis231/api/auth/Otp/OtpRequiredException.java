package com.csis231.api.auth.Otp;

import lombok.*;

@Getter
@Setter
@ToString
public class OtpRequiredException extends RuntimeException {

    private final String username;

    public OtpRequiredException(String username) {
        super("OTP required for user: " + username);
        this.username = username;
    }

}