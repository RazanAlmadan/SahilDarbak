package com.example.sahldarbak.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripBudgetEstimateDTO {

    private Double flightEstimate;

    private Double accommodationEstimate;

    private Double foodEstimate;

    private Double transportationEstimate;

    private Double activitiesEstimate;

    private String summary;
}