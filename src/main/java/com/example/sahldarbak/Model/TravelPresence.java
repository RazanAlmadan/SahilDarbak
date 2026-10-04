package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TravelPresence {

    @Id
    private Integer id;

    @NotEmpty(message = "country is required")
    @Column(columnDefinition = "varchar(50) not null")
    private String country;

    @NotEmpty(message = "city is required")
    @Column(columnDefinition = "varchar(50) not null")
    private String city;

//    @Column(columnDefinition = "date not null")
//    private LocalDate checkedInAt;

    @OneToOne
    @JsonIgnore
    @MapsId
    private User user;

}
