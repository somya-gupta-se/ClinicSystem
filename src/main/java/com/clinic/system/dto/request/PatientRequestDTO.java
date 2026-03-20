package com.clinic.system.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PatientRequestDTO(

        @NotBlank(message = "Full name in English is required")
        String fullNameEnglish,

        String fullNameArabic,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
        @NotBlank(message = "Mobile number is required")
        String mobileNumber,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @NotBlank(message = "National ID is required")
        String nationalId,

        @NotNull(message = "Address is required")
                @Valid
        AddressDTO address

) {

    public record AddressDTO(

            @NotBlank(message = "Street is required")
            String street,

            @NotBlank(message = "City is required")
            String city,

            @NotBlank(message = "Region is required")
            String region

    ) {}
}