package com.example.sahldarbak.DTO.SmartItinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SmartItineraryDTO {

    private List<ItineraryDayDTO> days;

    private List<PlaceRecommendationDTO> hotelRecommendations;

    private List<PlaceRecommendationDTO> restaurantRecommendations;
    private List<PlaceRecommendationDTO> activityRecommendations;
}