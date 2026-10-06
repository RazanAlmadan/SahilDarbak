package com.example.sahldarbak.DTO.SmartItinerary;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDTO {

    private Double estimatedPricePerNight;
    private String currency;
    private String source;
    private String sourceUrl;
}