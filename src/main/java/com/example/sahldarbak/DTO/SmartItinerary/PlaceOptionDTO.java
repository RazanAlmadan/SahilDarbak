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
public class PlaceOptionDTO {

    private String externalId;
    private String name;
    private String type;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private String websiteUrl;

    private List<RatingDTO> ratings;
    private HotelPriceDTO estimatedPrice;
    private HalalInfoDTO halalInfo;
}