package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
public class TravelMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "message is required")
    @Size(max = 300, message = "message must not exceed 300 characters")
    @Column(columnDefinition = "varchar(300) not null")
    private String message;

    @Pattern(regexp = "^(pending|accepted|rejected)$", message = "status must be pending, accepted, or rejected")
    @Column(columnDefinition = "varchar(10) not null")
    private String status;

    @Column(columnDefinition = "date not null", updatable = false)
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnore
    private User receiver;
}
