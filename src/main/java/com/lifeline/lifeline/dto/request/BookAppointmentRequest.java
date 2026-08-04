package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookAppointmentRequest {

    @NotBlank(message = "Slot ID is required")
    private String slotId;

    private String reasonForVisit;
}