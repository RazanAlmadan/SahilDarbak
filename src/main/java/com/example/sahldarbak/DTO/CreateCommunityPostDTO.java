package com.example.sahldarbak.DTO;


import jakarta.validation.constraints.NotEmpty;
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
}
