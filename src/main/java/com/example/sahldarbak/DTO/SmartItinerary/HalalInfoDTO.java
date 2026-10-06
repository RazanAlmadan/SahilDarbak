package com.example.sahldarbak.DTO.SmartItinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HalalInfoDTO {

    private String halalStatus; // VERIFIED | PARTIAL | NOT_VERIFIED
    private String evidence;
    private String source;
    private String sourceUrl;
}