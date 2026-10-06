package com.example.sahldarbak.DTO.DestinationRecommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {

    private String condition;

    private Double temperatureCelsius;

    private Boolean suitableForTrip;

    private String explanation;
}
