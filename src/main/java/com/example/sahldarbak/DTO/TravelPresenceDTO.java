package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "invalid latitude")
    @DecimalMax(value = "90.0", message = "invalid latitude")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "invalid longitude")
    @DecimalMax(value = "180.0", message = "invalid longitude")
    private Double longitude;
}
