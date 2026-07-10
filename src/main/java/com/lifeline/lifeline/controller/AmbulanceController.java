package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.RegisterAmbulanceRequest;
import com.lifeline.lifeline.dto.response.AmbulanceResponse;
import com.lifeline.lifeline.service.AmbulanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/ambulances")
@RequiredArgsConstructor
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<AmbulanceResponse> registerAmbulance(
            @PathVariable String hospitalId,
            @Valid @RequestBody RegisterAmbulanceRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ambulanceService.registerAmbulance(hospitalId, request, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<List<AmbulanceResponse>> getAmbulances(@PathVariable String hospitalId) {
        return ResponseEntity.ok(ambulanceService.getAmbulancesByHospital(hospitalId));
    }

    @PatchMapping("/{ambulanceId}/driver")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<AmbulanceResponse> assignDriver(
            @PathVariable String hospitalId,
            @PathVariable String ambulanceId,
            @RequestParam String driverUserId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ambulanceService.assignDriver(ambulanceId, driverUserId, authentication.getName())
        );
    }

    @PatchMapping("/{ambulanceId}/status")
    public ResponseEntity<AmbulanceResponse> updateStatus(
            @PathVariable String hospitalId,
            @PathVariable String ambulanceId,
            @RequestParam String status,
            Authentication authentication) {

        return ResponseEntity.ok(
                ambulanceService.updateStatus(ambulanceId, status, authentication.getName())
        );
    }
}