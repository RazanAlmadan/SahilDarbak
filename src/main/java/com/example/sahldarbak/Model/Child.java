package com.example.sahldarbak.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "child age is required")
    @Min(value = 0, message = "child age cannot be less than 0")
    @Max(value = 17, message = "child age must be less than 18")
    private Integer age;


    @ManyToOne
    @JoinColumn(name = "travel_request_id", nullable = false)
    @JsonIgnore
    private TravelRequest travelRequest;
}
