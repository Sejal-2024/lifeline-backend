package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.BookAppointmentRequest;
import com.lifeline.lifeline.dto.response.AppointmentResponse;
import com.lifeline.lifeline.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request, authentication.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(authentication.getName()));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(authentication.getName()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable String id,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, authentication.getName()));
    }
}