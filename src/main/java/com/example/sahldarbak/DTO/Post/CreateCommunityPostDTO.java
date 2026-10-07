package com.example.sahldarbak.DTO.Post;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommunityPostDTO {

    @NotEmpty(message = "title cannot be empty")
    private String title;

    @NotEmpty(message = "content cannot be empty")
    private String content;

    @NotEmpty(message = "country cannot be empty")
    private String country;

    @NotEmpty(message = "city cannot be empty")
    private String city;

    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;
}
