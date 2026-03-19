package com.clinic.system.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentRequestDTO(

        @NotNull(message = "Patient ID is required")
        @Positive(message = "Patient ID must be valid")
        Long patientId,

        @NotNull(message = "Doctor ID is required")
        @Positive(message = "Doctor ID must be valid")
        Long doctorId,

        @NotNull(message = "Appointment date is required")
        @Future(message = "Appointment must be in the future")
        LocalDate appointmentDate,

        @NotNull(message = "Appointment time is required")
        @Future(message = "Appointment must be in the future")
        LocalTime appointmentTime,

        String reason

) {}