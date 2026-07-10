package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.TriggerAlertRequest;
import com.lifeline.lifeline.dto.response.EmergencyAlertResponse;
import com.lifeline.lifeline.dto.response.HospitalResponse;
import com.lifeline.lifeline.dto.response.TriggerAlertResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.AlertStatus;
import com.lifeline.lifeline.model.EmergencyAlert;
import com.lifeline.lifeline.model.EmergencyContact;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.EmergencyAlertRepository;
import com.lifeline.lifeline.repository.EmergencyContactRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.EmailService;
import com.lifeline.lifeline.service.EmergencyAlertService;
import com.lifeline.lifeline.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyAlertServiceImpl implements EmergencyAlertService {

    private static final double NEARBY_HOSPITAL_RADIUS_KM = 20;

    private final EmergencyAlertRepository alertRepository;
    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;
    private final HospitalService hospitalService;
    private final EmailService emailService;

    @Override
    public TriggerAlertResponse triggerAlert(TriggerAlertRequest request, String patientEmail) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        GeoJsonPoint location = new GeoJsonPoint(request.getLongitude(), request.getLatitude());

        EmergencyAlert alert = EmergencyAlert.builder()
                .patientUserId(patient.getId())
                .location(location)
                .notes(request.getNotes())
                .status(AlertStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);

        List<HospitalResponse> nearbyHospitals = hospitalService.findNearbyHospitals(
                request.getLatitude(), request.getLongitude(), NEARBY_HOSPITAL_RADIUS_KM);

        notifyEmergencyContacts(patient, request);

        EmergencyAlertResponse alertResponse = toResponse(alert, patient.getFullName());

        return TriggerAlertResponse.builder()
                .alert(alertResponse)
                .nearbyHospitals(nearbyHospitals)
                .build();
    }

    private void notifyEmergencyContacts(User patient, TriggerAlertRequest request) {
        List<EmergencyContact> contacts = contactRepository.findByPatientUserId(patient.getId());

        String mapsLink = "https://www.google.com/maps?q=" + request.getLatitude() + "," + request.getLongitude();

        for (EmergencyContact contact : contacts) {
            if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
                emailService.sendEmergencyAlertNotification(
                        contact.getEmail(),
                        contact.getName(),
                        patient.getFullName(),
                        mapsLink,
                        request.getNotes()
                );
            }
        }
    }

    private EmergencyAlertResponse toResponse(EmergencyAlert alert, String patientName) {
        return EmergencyAlertResponse.builder()
                .id(alert.getId())
                .patientUserId(alert.getPatientUserId())
                .patientName(patientName)
                .latitude(alert.getLocation().getY())
                .longitude(alert.getLocation().getX())
                .notes(alert.getNotes())
                .status(alert.getStatus())
                .assignedHospitalId(alert.getAssignedHospitalId())
                .assignedAmbulanceId(alert.getAssignedAmbulanceId())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}