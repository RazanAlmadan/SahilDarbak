package com.example.sahldarbak.DTO.DestinationRecommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationRecommendationDTO {

    private String country;

    private String countryCode;

    private String capital;

    private int rank;

    private int suitabilityScore;

    private String recommendation;

    private WeatherDTO weather;

    private HolidayInfoDTO holidays;

    private CrowdDTO crowd;

    private String headsUp;

    private List<String> whyItMatches;
}
