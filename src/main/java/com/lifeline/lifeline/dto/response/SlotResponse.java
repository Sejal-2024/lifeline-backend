package com.lifeline.lifeline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private String id;
    private String doctorUserId;
    private String doctorName;
    private String hospitalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;
}