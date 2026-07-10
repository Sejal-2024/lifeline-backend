package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.DispatchAlertRequest;
import com.lifeline.lifeline.dto.request.TriggerAlertRequest;
import com.lifeline.lifeline.dto.response.EmergencyAlertResponse;
import com.lifeline.lifeline.dto.response.TriggerAlertResponse;
import com.lifeline.lifeline.service.EmergencyAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency-alerts")
@RequiredArgsConstructor
public class EmergencyAlertController {

    private final EmergencyAlertService alertService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<TriggerAlertResponse> triggerAlert(
            @Valid @RequestBody TriggerAlertRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(alertService.triggerAlert(request, authentication.getName()));
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<List<EmergencyAlertResponse>> getNearbyAlerts(
            @RequestParam(defaultValue = "20") double radiusKm,
            Authentication authentication) {
        return ResponseEntity.ok(alertService.getNearbyActiveAlerts(authentication.getName(), radiusKm));
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<EmergencyAlertResponse> dispatch(
            @PathVariable String id,
            @Valid @RequestBody DispatchAlertRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(alertService.dispatchAlert(id, request.getAmbulanceId(), authentication.getName()));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<EmergencyAlertResponse> resolve(
            @PathVariable String id,
            Authentication authentication) {
        return ResponseEntity.ok(alertService.resolveAlert(id, authentication.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<EmergencyAlertResponse>> getMyAlerts(Authentication authentication) {
        return ResponseEntity.ok(alertService.getMyAlerts(authentication.getName()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<EmergencyAlertResponse> cancel(
            @PathVariable String id,
            Authentication authentication) {
        return ResponseEntity.ok(alertService.cancelAlert(id, authentication.getName()));
    }
}