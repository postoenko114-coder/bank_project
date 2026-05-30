package com.alex.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    private String message;
}
