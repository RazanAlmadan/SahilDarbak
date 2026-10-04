package com.example.sahldarbak.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TripBudgetEstimateDTO {


    private Integer trip_id;

    @NotNull(message = "flight estimate cannot be null")
    private Double flightEstimate;

    @NotNull(message = "accommodation estimate cannot be null")
    private Double accommodationEstimate;

    @NotNull(message = "food estimate cannot be null")
    private Double foodEstimate;

    @NotNull(message = "transportation estimate cannot be null")
    private Double transportationEstimate;

    @NotNull(message = "activities estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double activitiesEstimate;

    @NotNull(message = "total estimate cannot be null")
    private Double totalEstimate;

    private LocalDate generatedAt;
}
