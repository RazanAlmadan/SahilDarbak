package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class TripBudgetEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "flight estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double flightEstimate;

    @NotNull(message = "accommodation estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double accommodationEstimate;

    @NotNull(message = "food estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double foodEstimate;

    @NotNull(message = "transportation estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double transportationEstimate;

    @NotNull(message = "activities estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double activitiesEstimate;

    @NotNull(message = "total estimate cannot be null")
    @Column(columnDefinition = "double not null")
    private Double totalEstimate;


    private LocalDate generatedAt;

    @OneToOne
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    @JsonIgnore
    private Trip trip;


}
