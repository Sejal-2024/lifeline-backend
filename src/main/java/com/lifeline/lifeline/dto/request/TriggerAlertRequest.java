package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TriggerAlertRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String notes;
}