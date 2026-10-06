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


    @Column(columnDefinition = "varchar(50) not null")
    private String country;


    @Column(columnDefinition = "varchar(50) not null")
    private String city;

    @Column(columnDefinition = "date not null")
    private LocalDate checkedInAt;

    @Column(columnDefinition = "double not null")
    private Double latitude;

    @Column(columnDefinition = "double not null")
    private Double longitude;

    @OneToOne
    @JsonIgnore
    @MapsId
    private User user;


    @OneToOne
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    private Trip trip;

}
