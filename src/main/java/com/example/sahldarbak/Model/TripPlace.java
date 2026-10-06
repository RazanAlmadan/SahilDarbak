package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TripPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "place type cannot be empty")
    @Column(columnDefinition = "varchar(50) not null")
    private String placeType;

    @NotEmpty(message = "name cannot be empty")
    @Column(columnDefinition = "varchar(50) not null")
    private String name;

    @NotEmpty(message = "city cannot be empty")
    @Column(columnDefinition = "varchar(50) not null")
    private String city;

    @NotNull(message = "scheduled at cannot be empty")
    @Column(columnDefinition = "date not null")
    private LocalDate scheduledAt;

    @Column(columnDefinition = "varchar(50)")
    private String notes;


    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    @JsonIgnore
    private Itinerary itinerary;
}
