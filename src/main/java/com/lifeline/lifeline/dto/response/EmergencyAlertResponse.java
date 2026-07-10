package com.lifeline.lifeline.dto.response;

import com.lifeline.lifeline.model.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlertResponse {
    private String id;
    private String patientUserId;
    private String patientName;
    private double latitude;
    private double longitude;
    private String notes;
    private AlertStatus status;
    private String assignedHospitalId;
    private String assignedAmbulanceId;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}