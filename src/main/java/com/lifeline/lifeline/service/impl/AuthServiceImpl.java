package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.LoginRequest;
import com.lifeline.lifeline.dto.request.RegisterRequest;
import com.lifeline.lifeline.dto.response.AuthResponse;
import com.lifeline.lifeline.dto.response.UserResponse;
import com.lifeline.lifeline.exception.DuplicateResourceException;
import com.lifeline.lifeline.exception.InvalidTokenException;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.Role;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.security.JwtUtil;
import com.lifeline.lifeline.service.AuthService;
import com.lifeline.lifeline.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("An account with this phone number already exists");
        }

        validateRoleSpecificFields(request);

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .enabled(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .hospitalId(request.getHospitalId())
                .vehicleId(request.getVehicleId())
                .build();

        userRepository.save(user);

        String verificationLink = baseUrl + "/api/v1/auth/verify?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException ex) {
            throw new DisabledException("Please verify your email before logging in");
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();


    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired. Please log in again.");
        }

        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    private void validateRoleSpecificFields(RegisterRequest request) {
        Role role = request.getRole();

        if (role == Role.DOCTOR) {
            require(request.getSpecialization(), "Specialization is required for doctors");
            require(request.getLicenseNumber(), "License number is required for doctors");
            require(request.getHospitalId(), "Hospital ID is required for doctors");
        }

        if (role == Role.AMBULANCE_DRIVER) {
            require(request.getLicenseNumber(), "License number is required for ambulance drivers");
            require(request.getVehicleId(), "Vehicle ID is required for ambulance drivers");
            require(request.getHospitalId(), "Hospital ID is required for ambulance drivers");
        }

        if (role == Role.HOSPITAL_ADMIN) {
            require(request.getHospitalId(), "Hospital ID is required for hospital admins");
        }
    }

    private void require(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }


}