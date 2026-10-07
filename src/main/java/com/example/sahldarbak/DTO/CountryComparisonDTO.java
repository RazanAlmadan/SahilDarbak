package com.example.sahldarbak.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryComparisonDTO {

    private String country1;
    private String country2;

    private String betterForWeather;
    private String betterForActivities;
    private String betterForEnvironment;
    private String betterForCrowds;
    private String betterForBudget;

    private String recommendation;
    private String reason;
}
