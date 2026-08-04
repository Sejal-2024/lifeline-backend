package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.BookAppointmentRequest;
import com.lifeline.lifeline.dto.response.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(BookAppointmentRequest request, String patientEmail);
    List<AppointmentResponse> getMyAppointments(String patientEmail);
    List<AppointmentResponse> getDoctorAppointments(String doctorEmail);
    AppointmentResponse cancelAppointment(String appointmentId, String requesterEmail);
}