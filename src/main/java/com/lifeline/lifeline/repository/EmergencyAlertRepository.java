package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.EmergencyAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends MongoRepository<EmergencyAlert, String> {

    List<EmergencyAlert> findByPatientUserId(String patientUserId);
}