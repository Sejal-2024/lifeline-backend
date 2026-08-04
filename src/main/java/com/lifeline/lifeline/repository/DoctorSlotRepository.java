package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.DoctorSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends MongoRepository<DoctorSlot, String> {

    List<DoctorSlot> findByDoctorUserIdAndIsBookedFalseAndStartTimeAfter(String doctorUserId, LocalDateTime after);
}