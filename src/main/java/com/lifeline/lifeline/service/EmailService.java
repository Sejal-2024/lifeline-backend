package com.lifeline.lifeline.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String verificationLink);
}