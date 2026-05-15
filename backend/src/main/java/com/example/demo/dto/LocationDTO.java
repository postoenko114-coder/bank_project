package com.example.demo.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private String city;

    private String address;

    private String country;

    private String postCode;

    private Double latitude;

    private Double longitude;
}
