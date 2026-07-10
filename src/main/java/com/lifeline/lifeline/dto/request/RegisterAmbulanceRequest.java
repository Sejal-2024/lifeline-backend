package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterAmbulanceRequest {

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    private String driverUserId; // optional — can be assigned later
}