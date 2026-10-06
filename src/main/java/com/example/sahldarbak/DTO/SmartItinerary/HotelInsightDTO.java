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
public class HotelInsightDTO {

    private List<RatingDTO> ratings;
    private HotelPriceDTO estimatedPrice;
}