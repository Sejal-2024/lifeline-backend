package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterHospitalRequest {

    @NotBlank(message = "Hospital name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    private String contactEmail;

    @Min(value = 1, message = "Total beds must be at least 1")
    private int totalBeds;
}