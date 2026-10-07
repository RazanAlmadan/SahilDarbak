package com.example.sahldarbak.AI;

import com.example.sahldarbak.DTO.DestinationRecommendation.DestinationRecommendationDTO;
import com.example.sahldarbak.DTO.DestinationRecommendation.TravelSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {

    private String status;

    private TravelSummaryDTO travelSummary;

    private List<DestinationRecommendationDTO> destinations;
}
