package com.example.sahldarbak.DTO.CityPlan;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityPlanItemDTO {

    private String city;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer cityOrder;

    private String reason;
}