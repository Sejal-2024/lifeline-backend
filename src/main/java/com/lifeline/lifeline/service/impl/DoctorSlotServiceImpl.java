package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.CreateSlotRequest;
import com.lifeline.lifeline.dto.response.SlotResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.DoctorSlot;
import com.lifeline.lifeline.model.User;
import com.lifeline.lifeline.repository.DoctorSlotRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorSlotServiceImpl implements DoctorSlotService {

    private final DoctorSlotRepository slotRepository;
    private final UserRepository userRepository;

    @Override
    public SlotResponse createSlot(CreateSlotRequest request, String doctorEmail) {

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        DoctorSlot slot = DoctorSlot.builder()
                .doctorUserId(doctor.getId())
                .hospitalId(doctor.getHospitalId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        slotRepository.save(slot);
        return toResponse(slot, doctor.getFullName());
    }

    @Override
    public List<SlotResponse> getAvailableSlots(String doctorUserId) {
        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return slotRepository.findByDoctorUserIdAndIsBookedFalseAndStartTimeAfter(doctorUserId, LocalDateTime.now())
                .stream()
                .map(slot -> toResponse(slot, doctor.getFullName()))
                .toList();
    }

    @Override
    public List<SlotResponse> getMySlots(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return slotRepository.findByDoctorUserIdAndIsBookedFalseAndStartTimeAfter(doctor.getId(), LocalDateTime.now())
                .stream()
                .map(slot -> toResponse(slot, doctor.getFullName()))
                .toList();
    }

    private SlotResponse toResponse(DoctorSlot slot, String doctorName) {
        return SlotResponse.builder()
                .id(slot.getId())
                .doctorUserId(slot.getDoctorUserId())
                .doctorName(doctorName)
                .hospitalId(slot.getHospitalId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.isBooked())
                .build();
    }
}