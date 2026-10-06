package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripCity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @NotEmpty(message = "city cannot be empty")
    @Size(min = 2, max = 50, message = "city cannot be less than 2 or more than 50")
    @Column(columnDefinition = "varchar(50) not null")
    private String city;


    @NotNull(message = "start date cannot be empty")
    @FutureOrPresent(message = "start date cannot be in the past")
    @Column(columnDefinition = "date not null")
    private LocalDate startDate;


    @NotNull(message = "end date cannot be empty")
    @FutureOrPresent(message = "end date cannot be in the past")
    @Column(columnDefinition = "date not null")
    private LocalDate endDate;


    @NotNull(message = "city order cannot be empty")
    @Positive(message = "city order must be greater than zero")
    @Column(columnDefinition = "int not null")
    private Integer cityOrder;


    @Pattern(regexp = "suggested|accepted", message = "status must be suggested or accepted")
    @Column(columnDefinition = "varchar(15) not null")
    private String status;


    @ManyToOne
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    private Trip trip;


}