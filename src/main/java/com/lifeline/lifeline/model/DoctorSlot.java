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
@Document(collection = "doctor_slots")
public class DoctorSlot {

    @Id
    private String id;

    private String doctorUserId;
    private String hospitalId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isBooked;
}