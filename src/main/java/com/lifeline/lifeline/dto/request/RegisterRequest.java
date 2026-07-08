package com.lifeline.lifeline.dto.request;

import com.lifeline.lifeline.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    // Role-specific — validated conditionally in the service layer, not here
    private String specialization;
    private String licenseNumber;
    private String hospitalId;
    private String vehicleId;
}