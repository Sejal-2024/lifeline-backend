package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.CreateSlotRequest;
import com.lifeline.lifeline.dto.response.SlotResponse;
import com.lifeline.lifeline.service.DoctorSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor-slots")
@RequiredArgsConstructor
public class DoctorSlotController {

    private final DoctorSlotService slotService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SlotResponse> createSlot(
            @Valid @RequestBody CreateSlotRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(slotService.createSlot(request, authentication.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<SlotResponse>> getMySlots(Authentication authentication) {
        return ResponseEntity.ok(slotService.getMySlots(authentication.getName()));
    }

    @GetMapping("/doctor/{doctorUserId}")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(@PathVariable String doctorUserId) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorUserId));
    }
}