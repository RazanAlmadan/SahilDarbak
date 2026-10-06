package com.example.sahldarbak.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearestTravelerDTO {
    private Integer userId;
    private String fullName;
    private Integer age;
    private String gender;
    private String homeCountry;
    private String homeCity;
    private String bio;
    private String currentCountry;
    private String currentCity;
    private Double distanceKm;
}