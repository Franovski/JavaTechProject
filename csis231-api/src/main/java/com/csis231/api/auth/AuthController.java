package com.csis231.api.auth;

import com.csis231.api.auth.Otp.OtpRequiredException;
import com.csis231.api.user.User;
import com.csis231.api.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse resp = authService.login(request);

            // Successful login — return JWT or user data
            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));

        } catch (OtpRequiredException e) {
            // OTP required (2FA flow)
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "otpRequired", true,
                            "purpose", "LOGIN_2FA",
                            "username", e.getUsername()
                    ));

        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Something went wrong"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            // Check if username or email already exists
            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Username already exists"));
            }

            if (userRepository.findByEmail(req.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already exists"));
            }

            // Build new user
            User u = User.builder()
                    .username(req.getUsername())
                    .email(req.getEmail())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .firstName(req.getFirstName())
                    .lastName(req.getLastName())
                    .phone(req.getPhone())
                    .role(parseRole(req.getRole()))
                    .build();

            userRepository.save(u);

            return ResponseEntity.ok(Map.of("message", "Registered successfully"));

        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Registration failed"));
        }
    }

    /**
     * Parses role safely; defaults to CUSTOMER if null or invalid.
     */
    private User.Role parseRole(String role) {
        if (role == null) return User.Role.CUSTOMER;
        try {
            return User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.Role.CUSTOMER;
        }
    }


    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        try {
            authService.requestPasswordReset(req); // sends PASSWORD_RESET OTP
            return ResponseEntity.ok(Map.of("message", "OTP sent"));
        } catch (BadCredentialsException e) {
            // avoid leaking which emails exist
            return ResponseEntity.ok(Map.of("message", "If the email exists, an OTP has been sent"));
        } catch (Exception e) {
            log.error("Forgot password error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Could not send reset code"));
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        try {
            authService.resetPassword(req);
            return ResponseEntity.ok(Map.of("message", "Password updated"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or code"));
        } catch (Exception e) {
            log.error("Reset password error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Could not reset password"));
        }
    }
}
