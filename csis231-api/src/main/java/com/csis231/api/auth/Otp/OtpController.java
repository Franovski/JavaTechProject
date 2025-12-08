package com.csis231.api.auth.Otp;

import com.csis231.api.auth.AuthResponse;
import com.csis231.api.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody OtpVerifyRequest req) {
        try {
            AuthResponse resp = authService.verifyOtp(req); // issues JWT on success
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.warn("OTP verify failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or code"));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> request(@Valid @RequestBody OtpResendRequest req) {
        authService.resendOtp(req.getUsername());
        return ResponseEntity.ok(Map.of("message", "OTP resent"));
    }
}
