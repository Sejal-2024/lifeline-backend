package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.EmergencyContactRequest;
import com.lifeline.lifeline.dto.response.EmergencyContactResponse;

import java.util.List;

public interface EmergencyContactService {
    EmergencyContactResponse addContact(EmergencyContactRequest request, String patientEmail);
    List<EmergencyContactResponse> getMyContacts(String patientEmail);
    EmergencyContactResponse updateContact(String contactId, EmergencyContactRequest request, String patientEmail);
    void deleteContact(String contactId, String patientEmail);
}