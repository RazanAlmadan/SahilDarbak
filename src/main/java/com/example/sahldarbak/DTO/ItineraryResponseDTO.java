package com.example.sahldarbak.DTO;


import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResponseDTO {

    private Integer itineraryId;
    private String status;
    private LocalDateTime generatedAt;
    private SmartItineraryDTO plan;
}