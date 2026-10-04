package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "restriction type is required")
    @Pattern(regexp = "^(allergy|accessibility|dietary|medical|other)$", message = "invalid restriction type")
    private String restrictionType;

    @NotEmpty(message = "description is required")
    @Column(columnDefinition = "varchar(255) not null")
    private String description;

    @NotNull(message = "is required must not be null")
    private Boolean isRequired;


    @ManyToOne
    @JoinColumn(name = "travel_request_id", nullable = false)
    @JsonIgnore
    private TravelRequest travelRequest;
}
