package com.example.sahldarbak.DTO.DestinationRecommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayInfoDTO {

    private Boolean hasHolidayDuringTrip;

    private int holidayCount;

    private List<HolidayDTO> holidays;
}
