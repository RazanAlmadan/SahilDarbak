package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {

    @NotNull(message = "user id cannot be null")
    private Integer userId;

    @NotEmpty(message = "full name is required")
    private String fullName;

    @NotNull(message = "date of birth is required")
    @Past(message = "date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "gender is required")
    @Pattern(regexp = "^(male|female)$", message = "gender must be male or female")
    private String gender;

    @NotEmpty(message = "country is required")
    private String country;

    @NotEmpty(message = "city is required")
    private String city;

    @Size(max = 500, message = "bio must not exceed 500 characters")
    private String bio;
}