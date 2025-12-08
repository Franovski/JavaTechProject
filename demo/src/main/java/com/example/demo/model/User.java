package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive = true;
    private Boolean emailVerified = false;
    private Boolean twoFactorEnabled = false;
    private Role role = Role.CUSTOMER;
    @JsonIgnore
    private Instant createdAt;

    public enum Role {
        ADMIN, CUSTOMER
    }

    @JsonProperty("createdAt")
    public String getCreatedAtString() {
        return (createdAt != null) ? createdAt.toString() : null;
    }

    @JsonProperty("createdAt")
    public void setCreatedAtString(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                this.createdAt = Instant.parse(value);
            } catch (DateTimeParseException e) {
                this.createdAt = null;
            }
        } else {
            this.createdAt = null;
        }
    }

    @JsonIgnore
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // === Added for TableView display ===
    @JsonIgnore
    public String getCreatedAtFormatted() {
        return (createdAt != null) ? createdAt.toString().replace("T", " ").replace("Z", "") : "";
    }
}
