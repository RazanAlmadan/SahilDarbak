package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(regexp = "suggested|accepted", message = "status must be suggested or accepted")
    @Column(columnDefinition = "varchar(15) not null")
    private String status;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime generatedAt;

    @Lob
    @Column(columnDefinition = "longtext")
    private String planJson;

    @OneToOne
    @JoinColumn(name = "trip_id", unique = true)
    @JsonIgnore
    private Trip trip;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TripPlace> tripPlaces;
}