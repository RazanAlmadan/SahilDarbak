package com.example.sahldarbak.DTO.SmartItinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;



@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRecommendationDTO {

    private String name;
    private String type;
    private String city;
    private LocalTime suggestedTime;
    private String reason;

    private List<RatingDTO> ratings;
    private HotelPriceDTO estimatedPrice;
    private HalalInfoDTO halalInfo;

    private String officialWebsite;
}