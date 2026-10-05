package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
public class Profile {

    @Id
    private Integer id;


    @Column(columnDefinition = "varchar(100) not null")
    private String fullName;



    @Column(columnDefinition = "date not null")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "varchar(10) not null")
    private String gender;


    @Column(columnDefinition = "varchar(50) not null")
    private String country;


    @Column(columnDefinition = "varchar(50) not null")
    private String city;


    @Column(columnDefinition = "varchar(500)")
    private String bio;

    @OneToOne
    @JsonIgnore
    @MapsId
    private User user;
}
