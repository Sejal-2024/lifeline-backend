package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(String toEmail, String fullName, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your LifeLine AI account");
        message.setText(
                "Hi " + fullName + ",\n\n" +
                        "Please verify your account by clicking the link below:\n" +
                        verificationLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "- LifeLine AI Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your LifeLine AI password");
        message.setText(
                "Hi " + fullName + ",\n\n" +
                        "We received a request to reset your password. Click the link below:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 1 hour. If you didn't request this, you can ignore this email.\n\n" +
                        "- LifeLine AI Team"
        );
        mailSender.send(message);
    }
}