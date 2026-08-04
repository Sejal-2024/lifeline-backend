package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByPatientUserId(String patientUserId);
    List<Appointment> findByDoctorUserId(String doctorUserId);
}