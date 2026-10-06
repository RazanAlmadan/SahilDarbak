package com.example.sahldarbak.DTO.SmartItinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryDayDTO {

    private Integer dayNumber;

    private LocalDate date;

    private List<PlaceRecommendationDTO> places;
}