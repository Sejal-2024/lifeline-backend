package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.EmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends MongoRepository<EmergencyContact, String> {

    List<EmergencyContact> findByPatientUserId(String patientUserId);

    long countByPatientUserId(String patientUserId);
}