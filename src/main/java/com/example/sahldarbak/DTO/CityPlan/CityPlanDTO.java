package com.example.sahldarbak.DTO.CityPlan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityPlanDTO {

    private List<CityPlanItemDTO> cities;
}