package com.lifeline.lifeline.dto.response;

import com.lifeline.lifeline.model.AmbulanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceResponse {
    private String id;
    private String vehicleNumber;
    private String hospitalId;
    private String driverUserId;
    private String driverName;   // convenience field — resolved from the driver's User record
    private AmbulanceStatus status;
    private Double latitude;
    private Double longitude;
}