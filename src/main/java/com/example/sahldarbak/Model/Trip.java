package com.example.sahldarbak.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

   // private User user;

   // private TravelRequest travelRequest;


    /// country and city can be inheart from DestinationRecommendation

//    @NotEmpty(message = "country cannot be empty")
//    @Size(min = 3, max = 30, message = "country size cannot be less than 3 or more than 30")
//    @Column(columnDefinition = "varchar(30) not null")
//    private String country;
//
//    @NotEmpty(message = "city cannot be null")
//    @Size(min = 3, max = 30, message = "city cannot be less than 3 and more than 30")
//    @Column(columnDefinition = "varchar(30) not null")
//    private String city;

    @NotEmpty(message = "start date cannot be empty")
    @FutureOrPresent(message = "start date cannot be in the past")
    @Column(columnDefinition = "date not null")
    private LocalDate startDate;

    @NotEmpty(message = "end date cannot be empty")
    @FutureOrPresent(message = "end date cannot be in the past")
    @Column(columnDefinition = "date not null")
    private LocalDate endDate;

    @NotNull(message = "budget cannot be empty")
    @Positive
    @Column(columnDefinition = "double not null")
    private Double budget;

    @NotEmpty(message = "status cannot be empty")
    @Column(columnDefinition = "varchar(30) not null")
    private String status;

    @NotEmpty
    private String travelType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "trip")
    private Set<TripPlace> tripPlace;










}
