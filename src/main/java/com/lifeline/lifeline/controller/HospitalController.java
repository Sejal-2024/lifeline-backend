package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.RegisterHospitalRequest;
import com.lifeline.lifeline.dto.request.UpdateBedsRequest;
import com.lifeline.lifeline.dto.response.HospitalResponse;
import com.lifeline.lifeline.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<HospitalResponse> registerHospital(
            @Valid @RequestBody RegisterHospitalRequest request,
            Authentication authentication) {

        String adminUserId = authentication.getName(); // this is the email, see note below
        return ResponseEntity.ok(hospitalService.registerHospital(request, adminUserId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<List<HospitalResponse>> getMyHospitals(Authentication authentication) {
        return ResponseEntity.ok(hospitalService.getMyHospitals(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponse> getHospitalById(@PathVariable String id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<HospitalResponse>> findNearbyHospitals(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        return ResponseEntity.ok(hospitalService.findNearbyHospitals(latitude, longitude, radiusKm));
    }

    @PatchMapping("/{id}/beds")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<HospitalResponse> updateBeds(
            @PathVariable String id,
            @Valid @RequestBody UpdateBedsRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                hospitalService.updateBedAvailability(id, request.getAvailableBeds(), authentication.getName())
        );
    }
}