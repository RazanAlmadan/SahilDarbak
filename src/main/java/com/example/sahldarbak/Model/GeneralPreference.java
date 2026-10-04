package com.example.sahldarbak.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralPreference {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "weather preference is required")
    @Pattern(regexp = "^(cold|mild|hot|any)$", message = "weather must be cold, mild, hot, or any")
    private String weather;

    @NotEmpty(message = "environment preference is required")
    @Pattern(regexp = "^(nature|city|beach|mixed|any)$", message = "environment must be nature, city, beach, mixed, or any")
    private String environment;

    @NotEmpty(message = "crowd preference is required")
    @Pattern(regexp = "^(low|medium|high|any)$", message = "crowd preference must be low, medium, high, or any")
    private String crowdPreference;

    @NotEmpty(message = "trip pace is required")
    @Pattern(regexp = "^(relaxed|moderate|active)$", message = "trip pace must be relaxed, moderate, or active")
    private String tripPace;


    @OneToOne
    @JoinColumn(name = "travel_request_id", nullable = false, unique = true)
    @JsonIgnore
    private TravelRequest travelRequest;
}
