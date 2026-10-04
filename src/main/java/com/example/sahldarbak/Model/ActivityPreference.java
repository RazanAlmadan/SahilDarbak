package com.example.sahldarbak.Model;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "activity type is required")
    @Pattern(regexp = "^(museum|hiking|shopping|beach|culture|entertainment|food|other)$", message = "invalid activity type")
    private String activityType;

    @Min(value = 1, message = "priority must be at least 1")
    @Max(value = 5, message = "priority must not exceed 5")
    private Integer priority;


    @ManyToOne
    @JoinColumn(name = "travel_request_id", nullable = false)
    @JsonIgnore
    private TravelRequest travelRequest;
}
