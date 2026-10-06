package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectDestinationDTO {

    @NotEmpty(message = "country cannot be empty")
    private String country;

    @NotEmpty(message = "city cannot be empty")
    private String city;
}
