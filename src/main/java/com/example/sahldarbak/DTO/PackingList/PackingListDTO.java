package com.example.sahldarbak.DTO.PackingList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackingListDTO {

    private String status;
    private String country;
    private String city;
    private String weatherSummary;
    private List<String> clothing;
    private List<String> shoes;
    private List<String> weatherEssentials;
    private List<String> activityEssentials;
    private List<String> travelEssentials;
    private List<String> healthAndPersonal;
    private List<String> tips;
}
