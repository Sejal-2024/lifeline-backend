package com.lifeline.lifeline.dto.response;

import com.lifeline.lifeline.model.HospitalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {
    private String id;
    private String name;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private double latitude;
    private double longitude;
    private int totalBeds;
    private int availableBeds;
    private HospitalStatus status;
}