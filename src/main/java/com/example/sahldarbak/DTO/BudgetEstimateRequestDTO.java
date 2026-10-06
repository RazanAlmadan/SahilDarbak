package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BudgetEstimateRequestDTO {

    @NotEmpty(message = "departure country cannot be empty")
    private String departureCountry;

    @NotEmpty(message = "departure city cannot be empty")
    private String departureCity;

    @NotEmpty(message = "currency cannot be empty")
    @Pattern(regexp = "[A-Z]{3}", message = "currency must be a valid 3-letter code")
    private String currency;
}