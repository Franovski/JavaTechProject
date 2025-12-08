package com.csis231.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
