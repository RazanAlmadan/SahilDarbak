package com.example.sahldarbak.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "title cannot be empty")
    @Column(columnDefinition = "varchar(100) not null")
    private String title;

    @NotEmpty(message = "content cannot be empty")
    @Column(columnDefinition = "text not null")
    private String content;

    @Column(columnDefinition = "varchar(50) not null")
    private String country;

    @Column(columnDefinition = "varchar(50) not null")
    private String city;


    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    @Column(columnDefinition = "int not null")
    private Integer rating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
