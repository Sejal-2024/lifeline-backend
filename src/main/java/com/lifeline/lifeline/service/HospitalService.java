package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.RegisterHospitalRequest;
import com.lifeline.lifeline.dto.response.HospitalResponse;

import java.util.List;

public interface HospitalService {
    HospitalResponse registerHospital(RegisterHospitalRequest request, String adminUserId);
    List<HospitalResponse> getMyHospitals(String adminUserId);
    HospitalResponse getHospitalById(String id);
    List<HospitalResponse> findNearbyHospitals(double latitude, double longitude, double radiusKm);
    HospitalResponse updateBedAvailability(String hospitalId, int availableBeds, String adminEmail);
}