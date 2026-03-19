package com.clinic.system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorRequestDTO {

    @NotBlank(message = "Name in English is required")
    private String nameEnglish;

    private String nameArabic;

    @NotBlank(message = "Specialty is required")
    private String specialty;

    @Min(value = 0, message = "Years of experience must be non-negative")
    private int yearsOfExperience;

    private int consultationDuration;
}

