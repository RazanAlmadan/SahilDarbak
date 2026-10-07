package com.example.sahldarbak.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @NotEmpty(message = "email is required")
    @Email(message = "invalid email format")
    private String email;

    @NotEmpty(message = "password is required")
    private String password;
}