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
import com.lifeline.lifeline.repository.*;
import com.lifeline.lifeline.service.EmailService;
import com.lifeline.lifeline.service.EmergencyAlertService;
import com.lifeline.lifeline.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import com.lifeline.lifeline.model.Ambulance;
import com.lifeline.lifeline.model.AmbulanceStatus;
import com.lifeline.lifeline.model.Hospital;
import com.lifeline.lifeline.repository.AmbulanceRepository;
import com.lifeline.lifeline.repository.HospitalRepository;
//import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.security.access.AccessDeniedException;
import com.lifeline.lifeline.model.Role;


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
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final MongoTemplate mongoTemplate;

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

    @Override
    public List<EmergencyAlertResponse> getNearbyActiveAlerts(String adminEmail, double radiusKm) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        List<Hospital> myHospitals = hospitalRepository.findByAdminUserId(admin.getId());
        if (myHospitals.isEmpty()) {
            throw new ResourceNotFoundException("You have no registered hospital");
        }

        GeoJsonPoint hospitalLocation = myHospitals.get(0).getLocation();
        double radiusInMeters = radiusKm * 1000;

        Criteria criteria = Criteria.where("location")
                .nearSphere(hospitalLocation)
                .maxDistance(radiusInMeters)
                .and("status").is(AlertStatus.ACTIVE);

        List<EmergencyAlert> alerts = mongoTemplate.find(new Query(criteria), EmergencyAlert.class);

        return alerts.stream()
                .map(alert -> {
                    User patient = userRepository.findById(alert.getPatientUserId()).orElse(null);
                    return toResponse(alert, patient != null ? patient.getFullName() : "Unknown");
                })
                .toList();
    }

    @Override
    public EmergencyAlertResponse dispatchAlert(String alertId, String ambulanceId, String adminEmail) {

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE alerts can be dispatched");
        }

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found"));

        Hospital hospital = hospitalRepository.findById(ambulance.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        String adminUserId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"))
                .getId();

        if (!hospital.getAdminUserId().equals(adminUserId)) {
            throw new AccessDeniedException("You do not manage this ambulance's hospital");
        }

        if (ambulance.getStatus() != AmbulanceStatus.AVAILABLE) {
            throw new IllegalArgumentException("Ambulance is not currently available");
        }

        alert.setStatus(AlertStatus.DISPATCHED);
        alert.setAssignedHospitalId(hospital.getId());
        alert.setAssignedAmbulanceId(ambulance.getId());
        alertRepository.save(alert);

        ambulance.setStatus(AmbulanceStatus.EN_ROUTE);
        ambulanceRepository.save(ambulance);

        User patient = userRepository.findById(alert.getPatientUserId()).orElse(null);
        return toResponse(alert, patient != null ? patient.getFullName() : "Unknown");
    }

    @Override
    public EmergencyAlertResponse resolveAlert(String alertId, String requesterEmail) {

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAssignedDriver = false;
        boolean isOwningAdmin = false;

        if (alert.getAssignedAmbulanceId() != null) {
            Ambulance ambulance = ambulanceRepository.findById(alert.getAssignedAmbulanceId()).orElse(null);
            if (ambulance != null) {
                isAssignedDriver = requester.getId().equals(ambulance.getDriverUserId());
            }
        }

        if (alert.getAssignedHospitalId() != null && requester.getRole() == Role.HOSPITAL_ADMIN) {
            Hospital hospital = hospitalRepository.findById(alert.getAssignedHospitalId()).orElse(null);
            isOwningAdmin = hospital != null && hospital.getAdminUserId().equals(requester.getId());
        }

        if (!isAssignedDriver && !isOwningAdmin) {
            throw new AccessDeniedException("You do not have permission to resolve this alert");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);

        if (alert.getAssignedAmbulanceId() != null) {
            ambulanceRepository.findById(alert.getAssignedAmbulanceId()).ifPresent(ambulance -> {
                ambulance.setStatus(AmbulanceStatus.AVAILABLE);
                ambulanceRepository.save(ambulance);
            });
        }

        User patient = userRepository.findById(alert.getPatientUserId()).orElse(null);
        return toResponse(alert, patient != null ? patient.getFullName() : "Unknown");
    }

    @Override
    public List<EmergencyAlertResponse> getMyAlerts(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return alertRepository.findByPatientUserId(patient.getId()).stream()
                .map(alert -> toResponse(alert, patient.getFullName()))
                .toList();
    }

    @Override
    public EmergencyAlertResponse cancelAlert(String alertId, String patientEmail) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if (!alert.getPatientUserId().equals(patient.getId())) {
            throw new AccessDeniedException("You can only cancel your own alerts");
        }

        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE alerts can be cancelled");
        }

        alert.setStatus(AlertStatus.CANCELLED);
        alertRepository.save(alert);

        return toResponse(alert, patient.getFullName());
    }
}