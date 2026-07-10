package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.TriggerAlertRequest;
import com.lifeline.lifeline.dto.response.TriggerAlertResponse;
import com.lifeline.lifeline.service.EmergencyAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}