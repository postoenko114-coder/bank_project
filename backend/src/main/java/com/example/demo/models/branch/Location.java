package com.example.demo.models.branch;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Location {

    private Double latitude;

    private Double longitude;

    private String city;

    private String address;

    private String country;

    private String postCode;

}
