package com.lifeline.lifeline.controller;

import com.lifeline.lifeline.dto.request.EmergencyContactRequest;
import com.lifeline.lifeline.dto.response.EmergencyContactResponse;
import com.lifeline.lifeline.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency-contacts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    @PostMapping
    public ResponseEntity<EmergencyContactResponse> addContact(
            @Valid @RequestBody EmergencyContactRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(contactService.addContact(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<EmergencyContactResponse>> getMyContacts(Authentication authentication) {
        return ResponseEntity.ok(contactService.getMyContacts(authentication.getName()));
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<EmergencyContactResponse> updateContact(
            @PathVariable String contactId,
            @Valid @RequestBody EmergencyContactRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(contactService.updateContact(contactId, request, authentication.getName()));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<String> deleteContact(
            @PathVariable String contactId,
            Authentication authentication) {
        contactService.deleteContact(contactId, authentication.getName());
        return ResponseEntity.ok("Emergency contact deleted successfully");
    }
}