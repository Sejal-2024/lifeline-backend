package com.lifeline.lifeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class Appointment {

    @Id
    private String id;

    private String patientUserId;
    private String doctorUserId;
    private String hospitalId;
    private String slotId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AppointmentStatus status;
    private String reasonForVisit;

    private LocalDateTime createdAt;
}