package com.lifeline.lifeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emergency_contacts")
public class EmergencyContact {

    @Id
    private String id;

    private String patientUserId;

    private String name;
    private String relationship;
    private String phoneNumber;
    private String email;

    @CreatedDate
    private LocalDateTime createdAt;
}