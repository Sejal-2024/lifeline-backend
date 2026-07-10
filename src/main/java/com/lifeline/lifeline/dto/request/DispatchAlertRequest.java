package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DispatchAlertRequest {
    @NotBlank(message = "Ambulance ID is required")
    private String ambulanceId;
}