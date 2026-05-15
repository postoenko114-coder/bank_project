package com.example.demo.dto;

import lombok.Data;

@Data
public class WorkingHourDTO {

    private String dayOfWeek;

    private String openTime;
    private String closeTime;

    public WorkingHourDTO(String dayOfWeek, String openTime, String closeTime) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}