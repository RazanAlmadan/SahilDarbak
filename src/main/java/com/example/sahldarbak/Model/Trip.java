package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @NotEmpty(message = "country cannot be empty")
    @Size(min = 2, max = 50, message = "country cannot be less than 2 or more than 50")
    @Column(columnDefinition = "varchar(50) not null")
    private String country;

    @NotEmpty(message = "city cannot be null")
    @Size(min = 2, max = 50, message = "city cannot be less than 2 or more than 50")
    @Column(columnDefinition = "varchar(50) not null")
    private String city;


    @NotEmpty(message = "status cannot be empty")
    @Column(columnDefinition = "varchar(30) not null")
    private String status;



    @OneToOne
    @JoinColumn(name = "travel_request_id", unique = true)
    @JsonIgnore
    private TravelRequest travelRequest;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "trip")
    private TripBudgetEstimate tripBudgetEstimate;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private User user;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "trip")
    private TravelPresence travelPresence;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TripCity> tripCities;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "trip")
    private Itinerary itinerary;


}
