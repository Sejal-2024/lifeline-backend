package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.EmergencyContactRequest;
import com.lifeline.lifeline.dto.response.EmergencyContactResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.EmergencyContact;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.EmergencyContactRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private static final int MAX_CONTACTS_PER_PATIENT = 5;

    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    public EmergencyContactResponse addContact(EmergencyContactRequest request, String patientEmail) {

        String patientUserId = resolveUserId(patientEmail);

        long currentCount = contactRepository.countByPatientUserId(patientUserId);
        if (currentCount >= MAX_CONTACTS_PER_PATIENT) {
            throw new IllegalArgumentException(
                    "You can only have up to " + MAX_CONTACTS_PER_PATIENT + " emergency contacts");
        }

        EmergencyContact contact = EmergencyContact.builder()
                .patientUserId(patientUserId)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        contactRepository.save(contact);
        return toResponse(contact);
    }

    @Override
    public List<EmergencyContactResponse> getMyContacts(String patientEmail) {
        String patientUserId = resolveUserId(patientEmail);
        return contactRepository.findByPatientUserId(patientUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EmergencyContactResponse updateContact(String contactId, EmergencyContactRequest request, String patientEmail) {

        EmergencyContact contact = getOwnedContactOrThrow(contactId, patientEmail);

        contact.setName(request.getName());
        contact.setRelationship(request.getRelationship());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setEmail(request.getEmail());

        contactRepository.save(contact);
        return toResponse(contact);
    }

    @Override
    public void deleteContact(String contactId, String patientEmail) {
        EmergencyContact contact = getOwnedContactOrThrow(contactId, patientEmail);
        contactRepository.delete(contact);
    }

    private EmergencyContact getOwnedContactOrThrow(String contactId, String patientEmail) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found"));

        String requesterId = resolveUserId(patientEmail);

        if (!contact.getPatientUserId().equals(requesterId)) {
            throw new AccessDeniedException("You do not have permission to modify this contact");
        }

        return contact;
    }

    private String resolveUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    private EmergencyContactResponse toResponse(EmergencyContact contact) {
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .relationship(contact.getRelationship())
                .phoneNumber(contact.getPhoneNumber())
                .email(contact.getEmail())
                .build();
    }
}