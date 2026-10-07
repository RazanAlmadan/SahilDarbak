package com.example.sahldarbak.DTO.DestinationRecommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelSummaryDTO {

    private int destinationCount;

    private String overallRecommendation;

    private String generalAdvice;
}
