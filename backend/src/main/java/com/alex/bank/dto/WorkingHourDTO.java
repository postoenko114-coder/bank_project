package com.alex.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHourDTO {

    private String dayOfWeek;

    private String openTime;
    private String closeTime;

}
