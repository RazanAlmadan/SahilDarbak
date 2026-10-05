package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPresenceDTO {
    @NotNull(message = "user id cannot be null")
    private Integer userId;

    @NotEmpty(message = "country is required")
    private String country;

    @NotEmpty(message = "city is required")
    private String city;
}
