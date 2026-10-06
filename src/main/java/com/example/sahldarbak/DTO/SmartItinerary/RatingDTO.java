package com.example.sahldarbak.DTO.SmartItinerary;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {

    private Double rating;
    private Double ratingScale;
    private Integer reviewCount;
    private String source;
    private String sourceUrl;
}