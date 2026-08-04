package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.BookAppointmentRequest;
import com.lifeline.lifeline.dto.response.AppointmentResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.Appointment;
import com.lifeline.lifeline.model.AppointmentStatus;
import com.lifeline.lifeline.model.DoctorSlot;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.AppointmentRepository;
import com.lifeline.lifeline.repository.DoctorSlotRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public AppointmentResponse bookAppointment(BookAppointmentRequest request, String patientEmail) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Atomic find-and-modify: only succeeds if the slot is STILL unbooked at the exact
        // moment this runs. Prevents two patients booking the same slot simultaneously.
        Query query = new Query(
                Criteria.where("id").is(request.getSlotId()).and("isBooked").is(false)
        );
        Update update = new Update().set("isBooked", true);

        DoctorSlot bookedSlot = mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(true), DoctorSlot.class
        );

        if (bookedSlot == null) {
            throw new IllegalArgumentException("This slot is no longer available");
        }

        User doctor = userRepository.findById(bookedSlot.getDoctorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Appointment appointment = Appointment.builder()
                .patientUserId(patient.getId())
                .doctorUserId(bookedSlot.getDoctorUserId())
                .hospitalId(bookedSlot.getHospitalId())
                .slotId(bookedSlot.getId())
                .startTime(bookedSlot.getStartTime())
                .endTime(bookedSlot.getEndTime())
                .status(AppointmentStatus.BOOKED)
                .reasonForVisit(request.getReasonForVisit())
                .createdAt(LocalDateTime.now())
                .build();

        appointmentRepository.save(appointment);

        return toResponse(appointment, patient.getFullName(), doctor.getFullName());
    }

    @Override
    public List<AppointmentResponse> getMyAppointments(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return appointmentRepository.findByPatientUserId(patient.getId()).stream()
                .map(this::enrichAndConvert)
                .toList();
    }

    @Override
    public List<AppointmentResponse> getDoctorAppointments(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return appointmentRepository.findByDoctorUserId(doctor.getId()).stream()
                .map(this::enrichAndConvert)
                .toList();
    }

    @Override
    public AppointmentResponse cancelAppointment(String appointmentId, String requesterEmail) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isPatient = requester.getId().equals(appointment.getPatientUserId());
        boolean isDoctor = requester.getId().equals(appointment.getDoctorUserId());

        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("You do not have permission to cancel this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException("Only BOOKED appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        slotRepository.findById(appointment.getSlotId())
                .ifPresent(slot -> {
                    slot.setBooked(false);
                    slotRepository.save(slot);
                });

        User patient = userRepository.findById(appointment.getPatientUserId()).orElse(null);
        User doctor = userRepository.findById(appointment.getDoctorUserId()).orElse(null);

        return toResponse(appointment,
                patient != null ? patient.getFullName() : "Unknown",
                doctor != null ? doctor.getFullName() : "Unknown");
    }

    private AppointmentResponse enrichAndConvert(Appointment appointment) {
        String patientName = userRepository.findById(appointment.getPatientUserId())
                .map(User::getFullName).orElse("Unknown");
        String doctorName = userRepository.findById(appointment.getDoctorUserId())
                .map(User::getFullName).orElse("Unknown");
        return toResponse(appointment, patientName, doctorName);
    }

    private AppointmentResponse toResponse(Appointment appointment, String patientName, String doctorName) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientUserId(appointment.getPatientUserId())
                .patientName(patientName)
                .doctorUserId(appointment.getDoctorUserId())
                .doctorName(doctorName)
                .hospitalId(appointment.getHospitalId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .reasonForVisit(appointment.getReasonForVisit())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}