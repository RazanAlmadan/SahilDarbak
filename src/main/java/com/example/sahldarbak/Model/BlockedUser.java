package com.example.sahldarbak.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
public class BlockedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "date not null", updatable = false)
    private LocalDate blockedAt;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    @JsonIgnore
    private User blocker;

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    @JsonIgnore
    private User blocked;
}
