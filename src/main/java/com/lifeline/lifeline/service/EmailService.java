package com.lifeline.lifeline.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String verificationLink);
    void sendPasswordResetEmail(String toEmail, String fullName, String resetLink);
}