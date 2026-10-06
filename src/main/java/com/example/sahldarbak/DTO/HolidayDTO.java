package com.example.sahldarbak.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDTO {

    private String name;

    private String date;

    private String type;
}