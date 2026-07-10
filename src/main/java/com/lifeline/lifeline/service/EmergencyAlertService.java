package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.TriggerAlertRequest;
import com.lifeline.lifeline.dto.response.TriggerAlertResponse;

public interface EmergencyAlertService {
    TriggerAlertResponse triggerAlert(TriggerAlertRequest request, String patientEmail);
}