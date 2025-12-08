// com.csis231.api.auth.ForgotPasswordRequest.java
package com.csis231.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Email @NotBlank String email
) {}
