package com.csis231.api.auth.Otp;

import com.csis231.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpCodeRepository repo;
    private final JavaMailSender mailSender;

    @Value("georgefrangieh12@gmail.com")
    private String from;


    // Default variant (keeps your existing login OTP flow working)
    @Transactional
    public String createAndSend(User user, String purpose) {
        return createAndSend(user, purpose, 5, "Your OTP code", null);
    }

    // New flexible variant used by password reset
    @Transactional
    public String createAndSend(User user, String purpose, int ttlMinutes,
                                String subject, String body /* optional, can be null */) {
        // Invalidate active codes for this user/purpose
        List<OtpCode> actives = repo.findActiveByUserIdAndPurpose(user.getId(), purpose, Instant.now());

        actives.forEach(c -> c.setConsumedAt(Instant.now()));

        // Generate 6-digit code
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));

        // Persist
        OtpCode entity = OtpCode.builder()
                .user(user)
                .code(code)
                .purpose(purpose)
                .expiresAt(Instant.now().plusSeconds(ttlMinutes * 60L))
                .build();
        repo.save(entity);

        // Email
        try {
            if (from != null && !from.isBlank() && user.getEmail() != null) {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(from);
                msg.setTo(user.getEmail());
                msg.setSubject(subject != null ? subject : "Your OTP code");
                msg.setText(body != null ? body : ("Your one-time code is: " + code + " (valid " + ttlMinutes + " minutes)"));
                mailSender.send(msg);
            }
        } catch (Exception ex) {
            log.info("OTP email not sent (dev/local is fine). {}", ex.toString());
        }

        log.info("OTP for user={} purpose={} CODE={}", user.getUsername(), purpose, code);
        return code;
    }


    @Transactional
    public void sendPasswordResetOtp(User user) {
        int ttlMinutes = 10;
        String code = createAndSend(user, OtpPurposes.PASSWORD_RESET, ttlMinutes,
                "Password Reset Code",
                null );


        try {
            if (from != null && !from.isBlank() && user.getEmail() != null) {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(from);
                msg.setTo(user.getEmail());
                msg.setSubject("Password Reset Code");
                msg.setText("Use this code to reset your password (valid " + ttlMinutes + " minutes): " + code);
                mailSender.send(msg);
            }
        } catch (Exception ex) {
            log.warn("Could not send password reset email to {} (will still allow reset with code): {}", user.getEmail(), ex.toString());
        }

        log.info("Password reset OTP for user={} CODE={}", user.getUsername(), code);
    }



    @Transactional
    public boolean verify(User user, String purpose, String code) {
        Instant now = Instant.now();

        Optional<OtpCode> latest = repo.findTopByUser_IdAndPurposeOrderByIdDesc(user.getId(), purpose);

        if (latest.isEmpty()) return false;

        OtpCode c = latest.get();
        boolean ok = c.getConsumedAt() == null
                && now.isBefore(c.getExpiresAt())
                && c.getCode().equals(code);

        if (ok) {
            c.setConsumedAt(now);     // consume it
        }
        return ok;
    }

    @Transactional
    public void verifyOtpOrThrow(User user, String purpose, String code) {
        Instant now = Instant.now();

        OtpCode latest = repo.findTopByUser_IdAndPurposeOrderByIdDesc(user.getId(), purpose)
                .orElseThrow(() -> new OtpRequiredException("Invalid email or code"));

        boolean invalid = latest.getConsumedAt() != null
                || now.isAfter(latest.getExpiresAt())
                || !latest.getCode().equals(code);

        if (invalid) {
            throw new OtpRequiredException("Invalid email or code");
        }

        // consume the code
        latest.setConsumedAt(now);
        repo.save(latest);
    }




}
