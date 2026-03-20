package com.clinic.system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


public record DoctorRequestDTO (

    @NotBlank(message = "Name in English is required")
    String nameEnglish,

     String nameArabic,

    @NotBlank(message = "Specialty is required")
     String specialty,

    @Min(value = 0, message = "Years of experience must be non-negative")
     int yearsOfExperience,

     int consultationDuration){
}

