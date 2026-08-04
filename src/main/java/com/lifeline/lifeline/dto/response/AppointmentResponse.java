package com.lifeline.lifeline.dto.response;

import com.lifeline.lifeline.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String id;
    private String patientUserId;
    private String patientName;
    private String doctorUserId;
    private String doctorName;
    private String hospitalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String reasonForVisit;
    private LocalDateTime createdAt;
}