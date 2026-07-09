package com.lifeline.lifeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private Role role;

    private boolean enabled;

    // ---- Email verification ----
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    @CreatedDate
    private LocalDateTime createdAt;

    // ---- Role-specific fields (nullable, populated based on role) ----
    private String specialization;
    private String licenseNumber;
    private String hospitalId;
    private String vehicleId;


    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;
}