package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.RegisterAmbulanceRequest;
import com.lifeline.lifeline.dto.response.AmbulanceResponse;

import java.util.List;

public interface AmbulanceService {
    AmbulanceResponse registerAmbulance(String hospitalId, RegisterAmbulanceRequest request, String adminEmail);
    List<AmbulanceResponse> getAmbulancesByHospital(String hospitalId);
    AmbulanceResponse assignDriver(String ambulanceId, String driverUserId, String adminEmail);
    AmbulanceResponse updateStatus(String ambulanceId, String status, String requesterEmail);
}