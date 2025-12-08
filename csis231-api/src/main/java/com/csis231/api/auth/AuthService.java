package com.csis231.api.auth;

import com.csis231.api.auth.Otp.OtpPurposes;
import com.csis231.api.auth.Otp.OtpRequiredException;
import com.csis231.api.auth.Otp.OtpService;
import com.csis231.api.user.User;
import com.csis231.api.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // fetch by username first, then email (your frontend sends either)
        User user = userRepository.findByUsername(req.getUsername())
                .orElseGet(() -> userRepository.findByEmail(req.getUsername())
                        .orElseThrow(() -> new BadCredentialsException("Invalid username or password")));

        // Always require OTP for login (or gate by a flag user.isTwoFactorEnabled())
        boolean requiresLoginOtp = true;
        if (requiresLoginOtp) {
            otpService.createAndSend(user, OtpPurposes.LOGIN_2FA);
            // Important: do not return a JWT here.
            throw new OtpRequiredException(user.getUsername());
        }

        // If you ever disable OTP for some users:
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    /** Verify login OTP → returns AuthResponse with JWT */
    public AuthResponse verifyOtp(com.csis231.api.auth.Otp.OtpVerifyRequest req) {
        String id = req.getUsername();
        String code = req.getCode();

        User user = userRepository.findByUsername(id)
                .orElseGet(() -> userRepository.findByEmail(id)
                        .orElseThrow(() -> new BadCredentialsException("Invalid email or code")));

        otpService.verifyOtpOrThrow(user, OtpPurposes.LOGIN_2FA, code);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    /** Resend login OTP */
    public void resendOtp(String username) {
        if (username == null || username.isBlank()) return;
        userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .ifPresent(u -> otpService.createAndSend(u, OtpPurposes.LOGIN_2FA));
    }

    /** Forgot password: issue PASSWORD_RESET OTP to the email */
    public void requestPasswordReset(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Unknown email"));
        otpService.createAndSend(user, OtpPurposes.PASSWORD_RESET);
    }

    /** Reset password after verifying PASSWORD_RESET OTP */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Unknown email"));
        otpService.verifyOtpOrThrow(user, OtpPurposes.PASSWORD_RESET, req.code());
        user.setPassword(passwordEncoder.encode(req.newPassword()));
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String generateTokenFor(String username) {
        return jwtUtil.generateToken(username);
    }
}
