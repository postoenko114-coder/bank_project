package com.alex.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private String city;

    private String address;

    private String country;

    private String postCode;

    private Double latitude;

    private Double longitude;
}
