package com.csis231.api.auth.Otp;

import com.csis231.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EmailOtpSender implements OtpSender {

    private final JavaMailSender mailSender;

    @Override
    public void send(User user, String purpose, String code, Instant expiresAt) {
        // subject based on purpose
        String subject = switch (purpose) {
            case OtpPurposes.LOGIN_2FA -> "Your login verification code";
            case OtpPurposes.PASSWORD_RESET -> "Your password reset code";
            default -> "Your verification code";
        };

        String expiresAtLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(expiresAt);

        // plain text body
        String text = "Hello " + (user.getFirstName() != null ? user.getFirstName() : user.getUsername()) + ",\n\n"
                + "Your one-time code is: " + code + "\n"
                + "It will expire at: " + expiresAtLocal + "\n\n"
                + "If you did not request this, you can ignore this email.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());              // <- send to the user who is logging in
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}
