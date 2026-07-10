package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.TriggerAlertRequest;
import com.lifeline.lifeline.dto.response.EmergencyAlertResponse;
import com.lifeline.lifeline.dto.response.TriggerAlertResponse;

import java.util.List;

public interface EmergencyAlertService {
    TriggerAlertResponse triggerAlert(TriggerAlertRequest request, String patientEmail);
    List<EmergencyAlertResponse> getNearbyActiveAlerts(String adminEmail, double radiusKm);
    EmergencyAlertResponse dispatchAlert(String alertId, String ambulanceId, String adminEmail);
    EmergencyAlertResponse resolveAlert(String alertId, String requesterEmail);
    List<EmergencyAlertResponse> getMyAlerts(String patientEmail);
    EmergencyAlertResponse cancelAlert(String alertId, String patientEmail);
}