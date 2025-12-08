// LoginRequest.java
package com.csis231.api.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
