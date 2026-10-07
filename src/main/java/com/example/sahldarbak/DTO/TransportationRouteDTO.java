package com.example.sahldarbak.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransportationRouteDTO {

    private String from;
    private String to;

    private String recommendedMethod;

    private String transportType;

    private String line;

    private String instructions;

    private Double distanceKm;

    private Integer durationMinutes;

    private String reason;
}
