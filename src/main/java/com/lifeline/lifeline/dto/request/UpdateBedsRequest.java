package com.lifeline.lifeline.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateBedsRequest {
    @Min(value = 0, message = "Available beds cannot be negative")
    private int availableBeds;
}