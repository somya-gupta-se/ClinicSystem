package com.clinic.system.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "Username or Email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password

) {}