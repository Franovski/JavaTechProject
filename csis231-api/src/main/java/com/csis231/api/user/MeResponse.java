package com.csis231.api.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponse {
    private Long   id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;

    public String getFullName() {
        String fn = (firstName == null ? "" : firstName.trim());
        String ln = (lastName  == null ? "" : lastName.trim());
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? username : full;
    }
}