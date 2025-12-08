package com.csis231.api.auth.Otp;

import com.csis231.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


    @Entity
    @Table(
            name = "otp_codes",
            indexes = @Index(columnList = "user_id,purpose,expires_at")
    )
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class OtpCode {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(nullable = false, length = 6)
        private String code;

        @Column(nullable = false, length = 20)
        private String purpose; // e.g. LOGIN_2FA, EMAIL_VERIFY

        @Column(name = "expires_at", nullable = false)
        private Instant expiresAt;

        @Column(name = "consumed_at")
        private Instant consumedAt;

        public boolean isValidNow() {
            return consumedAt == null && Instant.now().isBefore(expiresAt);
        }
    }



