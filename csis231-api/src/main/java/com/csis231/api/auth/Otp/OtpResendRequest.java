package com.csis231.api.auth.Otp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpResendRequest {
    @NotBlank private String username;
    private String purpose = "LOGIN_2FA";
}
