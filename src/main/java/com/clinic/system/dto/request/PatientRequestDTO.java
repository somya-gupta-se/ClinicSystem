package com.clinic.system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDTO {

    @NotBlank(message = "Full name in English is required")
    private String fullNameEnglish;

    private String fullNameArabic;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @NotNull(message = "Address is required")
    private AddressDTO address;

    @Data
    public static class AddressDTO {
        private String street;
        private String city;
        private String region;
    }
}
