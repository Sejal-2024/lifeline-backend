package com.lifeline.lifeline.service;

import com.lifeline.lifeline.dto.request.CreateSlotRequest;
import com.lifeline.lifeline.dto.response.SlotResponse;

import java.util.List;

public interface DoctorSlotService {
    SlotResponse createSlot(CreateSlotRequest request, String doctorEmail);
    List<SlotResponse> getAvailableSlots(String doctorUserId);
    List<SlotResponse> getMySlots(String doctorEmail);
}