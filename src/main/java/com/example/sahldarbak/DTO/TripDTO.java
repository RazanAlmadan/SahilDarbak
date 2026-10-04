package com.example.sahldarbak.DTO;

import com.example.sahldarbak.Model.TripBudgetEstimate;
import com.example.sahldarbak.Model.TripPlace;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
public class TripDTO {


    private Integer travelRequest_id;

    // private User user;


    @NotEmpty(message = "country cannot be empty")
    @Size(min = 3, max = 30, message = "country size cannot be less than 3 or more than 30")
    private String country;

    @NotEmpty(message = "city cannot be null")
    @Size(min = 3, max = 30, message = "city cannot be less than 3 and more than 30")
    private String city;

    @NotEmpty(message = "start date cannot be empty")
    @FutureOrPresent(message = "start date cannot be in the past")
    private LocalDate startDate;

    @NotEmpty(message = "end date cannot be empty")
    @FutureOrPresent(message = "end date cannot be in the past")
    private LocalDate endDate;

    @NotNull(message = "budget cannot be empty")
    @Positive
    private Double budget;

    @NotEmpty(message = "status cannot be empty")
    private String status;

    @NotEmpty
    private String travelType;


}
