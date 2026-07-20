package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.RegisterAmbulanceRequest;
import com.lifeline.lifeline.dto.response.AmbulanceResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.Ambulance;
import com.lifeline.lifeline.model.AmbulanceStatus;
import com.lifeline.lifeline.model.Hospital;
import com.lifeline.lifeline.model.Role;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.AmbulanceRepository;
import com.lifeline.lifeline.repository.HospitalRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.AmbulanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmbulanceServiceImpl implements AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public AmbulanceResponse registerAmbulance(String hospitalId, RegisterAmbulanceRequest request, String adminEmail) {

        Hospital hospital = getOwnedHospitalOrThrow(hospitalId, adminEmail);

        if (request.getDriverUserId() != null) {
            validateDriver(request.getDriverUserId(), hospitalId);
        }

        Ambulance ambulance = Ambulance.builder()
                .vehicleNumber(request.getVehicleNumber())
                .hospitalId(hospital.getId())
                .driverUserId(request.getDriverUserId())
                .status(AmbulanceStatus.AVAILABLE)
                .build();

        ambulanceRepository.save(ambulance);
        return toResponse(ambulance);
    }

    @Override
    public List<AmbulanceResponse> getAmbulancesByHospital(String hospitalId) {
        return ambulanceRepository.findByHospitalId(hospitalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AmbulanceResponse assignDriver(String ambulanceId, String driverUserId, String adminEmail) {

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found"));

        getOwnedHospitalOrThrow(ambulance.getHospitalId(), adminEmail);
        validateDriver(driverUserId, ambulance.getHospitalId());

        ambulance.setDriverUserId(driverUserId);
        ambulanceRepository.save(ambulance);
        return toResponse(ambulance);
    }

    @Override
    public AmbulanceResponse updateStatus(String ambulanceId, String status, String requesterEmail) {

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAssignedDriver = requester.getId().equals(ambulance.getDriverUserId());
        boolean isOwningAdmin = requester.getRole() == Role.HOSPITAL_ADMIN
                && isHospitalOwnedBy(ambulance.getHospitalId(), requester.getId());

        if (!isAssignedDriver && !isOwningAdmin) {
            throw new AccessDeniedException("You do not have permission to update this ambulance's status");
        }

        AmbulanceStatus newStatus;
        try {
            newStatus = AmbulanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Must be one of: AVAILABLE, EN_ROUTE, BUSY, OFFLINE");
        }

        ambulance.setStatus(newStatus);
        ambulanceRepository.save(ambulance);
        return toResponse(ambulance);
    }

    private Hospital getOwnedHospitalOrThrow(String hospitalId, String adminEmail) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        String adminUserId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"))
                .getId();

        if (!hospital.getAdminUserId().equals(adminUserId)) {
            throw new AccessDeniedException("You do not manage this hospital");
        }

        return hospital;
    }

    private boolean isHospitalOwnedBy(String hospitalId, String userId) {
        return hospitalRepository.findById(hospitalId)
                .map(h -> h.getAdminUserId().equals(userId))
                .orElse(false);
    }

    private void validateDriver(String driverUserId, String hospitalId) {
        User driver = userRepository.findById(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driver.getRole() != Role.AMBULANCE_DRIVER) {
            throw new IllegalArgumentException("Assigned user is not an ambulance driver");
        }

        if (!hospitalId.equals(driver.getHospitalId())) {
            throw new IllegalArgumentException("Driver does not belong to this hospital");
        }
    }

    private AmbulanceResponse toResponse(Ambulance ambulance) {
        String driverName = null;
        if (ambulance.getDriverUserId() != null) {
            driverName = userRepository.findById(ambulance.getDriverUserId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        Double lat = null;
        Double lng = null;
        if (ambulance.getCurrentLocation() != null) {
            lat = ambulance.getCurrentLocation().getY();
            lng = ambulance.getCurrentLocation().getX();
        }

        return AmbulanceResponse.builder()
                .id(ambulance.getId())
                .vehicleNumber(ambulance.getVehicleNumber())
                .hospitalId(ambulance.getHospitalId())
                .driverUserId(ambulance.getDriverUserId())
                .driverName(driverName)
                .status(ambulance.getStatus())
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    @Override
    public AmbulanceResponse updateLocation(String ambulanceId, double latitude, double longitude, String driverEmail) {

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found"));

        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!driver.getId().equals(ambulance.getDriverUserId())) {
            throw new AccessDeniedException("You are not the assigned driver for this ambulance");
        }

        ambulance.setCurrentLocation(new org.springframework.data.mongodb.core.geo.GeoJsonPoint(longitude, latitude));
        ambulanceRepository.save(ambulance);

        AmbulanceResponse response = toResponse(ambulance);

        messagingTemplate.convertAndSend("/topic/ambulance/" + ambulanceId, response);

        return response;
    }
}