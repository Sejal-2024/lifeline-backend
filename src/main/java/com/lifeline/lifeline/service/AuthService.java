package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.LoginRequest;
import com.lifeline.lifeline.dto.request.RegisterRequest;
import com.lifeline.lifeline.dto.response.AuthResponse;
import com.lifeline.lifeline.dto.response.UserResponse;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyEmail(String token);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
}