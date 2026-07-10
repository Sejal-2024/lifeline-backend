package com.lifeline.lifeline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {
    private String id;
    private String name;
    private String relationship;
    private String phoneNumber;
    private String email;
}