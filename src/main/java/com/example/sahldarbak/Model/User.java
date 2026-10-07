package com.example.sahldarbak.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "email is required")
    @Email(message = "invalid email format")
    @Column(columnDefinition = "varchar(100) not null unique")
    private String email;


    @NotEmpty(message = "phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "phone must be in international format, e.g. +9665XXXXXXXX")
    @Column(columnDefinition = "varchar(16) not null unique")
    private String phoneNumber;

    @NotEmpty(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    @Column(columnDefinition = "varchar(255) not null")
    private String password;


    @PastOrPresent(message = "created at time can't be in the future")
    @Column(columnDefinition = "date not null", updatable = false)
    private LocalDate createdAt;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    @PrimaryKeyJoinColumn
    private Profile profile;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private TravelPresence travelPresence;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocker")
    private Set<BlockedUser> blockedUsers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocked")
    private Set<BlockedUser> blockedBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sender" )
    private Set<TravelMatch> sentMatches;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receiver")
    private Set<TravelMatch> receivedMatches;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Trip> trips;

    @OneToMany(mappedBy = "user")
    private Set<TravelRequest> travelRequests;

    @OneToMany(mappedBy = "user")
    private Set<CommunityPost> communityPosts;

}
