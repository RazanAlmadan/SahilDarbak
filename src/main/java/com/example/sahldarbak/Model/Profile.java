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

    @NotEmpty(message = "full name is required")
    @Column(columnDefinition = "varchar(100) not null")
    private String fullName;

    @NotNull(message = "date of birth is required")
    @Past(message = "date of birth must be in the past")
    @Column(columnDefinition = "date not null")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "gender is required")
    @Pattern(regexp = "^(male|female)$", message = "gender must be male or female")
    @Column(columnDefinition = "varchar(10) not null")
    private String gender;

    @NotEmpty(message = "country is required")
    @Column(columnDefinition = "varchar(50) not null")
    private String country;

    @NotEmpty(message = "city is required")
    @Column(columnDefinition = "varchar(50) not null")
    private String city;

    @Size(max = 500, message = "bio must not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String bio;

    @OneToOne
    @JsonIgnore
    @MapsId
    private User user;
}
