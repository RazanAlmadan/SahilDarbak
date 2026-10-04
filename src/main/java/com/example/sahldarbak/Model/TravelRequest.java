package com.example.sahldarbak.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "start date is required")
    @FutureOrPresent(message = "start date must be today or in the future")
    @Column(columnDefinition = "date not null")
    private LocalDate startDate;

    @NotNull(message = "end date is required")
    @Future(message = "end date must be in the future")
    @Column(columnDefinition = "date not null")
    private LocalDate endDate;

    @NotNull(message = "budget is required")
    @Positive(message = "budget must be greater than zero")
    @Column(nullable = false)
    private Double budget;

    @NotEmpty(message = "travel type is required")
    @Pattern(regexp = "^(solo|group|family)$", message = "travel type must be solo, group, or family")
    @Column(columnDefinition = "varchar(10) not null")
    private String travelType;

    @Positive(message = "group size must be greater than zero")
    private Integer groupSize;

    @Positive(message = "adults count must be greater than zero")
    private Integer adultsCount;

    @Pattern(regexp = "^(open|completed|cancelled)$", message = "status must be open, completed, or cancelled")
    @Column(columnDefinition = "varchar(10) not null")
    private String status;


    @OneToOne(mappedBy = "travelRequest", cascade = CascadeType.ALL)
    private GeneralPreference generalPreference;


    @OneToMany(mappedBy = "travelRequest", cascade = CascadeType.ALL)
    private Set<FoodPreference> foodPreferences;


    @OneToMany(mappedBy = "travelRequest", cascade = CascadeType.ALL)
    private Set<ActivityPreference> activityPreferences;


    @OneToMany(mappedBy = "travelRequest", cascade = CascadeType.ALL)
    private Set<TravelRestriction> travelRestrictions;


    @OneToMany(mappedBy = "travelRequest", cascade = CascadeType.ALL)
    private Set<Child> children;

    @OneToOne(mappedBy = "travelRequest")
    private Trip trip;
}
