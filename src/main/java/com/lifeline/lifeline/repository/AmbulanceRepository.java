package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.Ambulance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbulanceRepository extends MongoRepository<Ambulance, String> {

    List<Ambulance> findByHospitalId(String hospitalId);
}