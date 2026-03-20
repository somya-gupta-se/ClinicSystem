package com.clinic.system.controller;

import com.clinic.system.dto.request.AppointmentRequestDTO;
import com.clinic.system.dto.response.ApiResponseDTO;
import com.clinic.system.dto.response.AppointmentResponseDTO;
import com.clinic.system.entity.Appointment;
import com.clinic.system.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinic.system.dto.response.PatientResponseDTO;
import com.clinic.system.dto.response.DoctorResponseDTO;


import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponseDTO<AppointmentResponseDTO>> create(@Valid @RequestBody AppointmentRequestDTO dto) {
        Appointment appointment = service.scheduleAppointment(dto);
        AppointmentResponseDTO response = mapToAppointmentResponseDTO(appointment);
        log.info("Scheduling appointment for patient {}", dto.patientId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, "Appointment scheduled successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AppointmentResponseDTO>>> getAllAppointments() {
        List<Appointment> appointments = service.getAllAppointments();
        List<AppointmentResponseDTO> response = appointments.stream()
                .map(this::mapToAppointmentResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Appointments retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AppointmentResponseDTO>> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = service.getAppointmentById(id);
        AppointmentResponseDTO response = mapToAppointmentResponseDTO(appointment);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Appointment retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AppointmentResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO dto) {
        Appointment appointment = service.updateAppointment(id, dto);
        AppointmentResponseDTO response = mapToAppointmentResponseDTO(appointment);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Appointment updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAppointment(@PathVariable Long id) {
        service.deleteAppointment(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Appointment deleted successfully"));
    }

    private AppointmentResponseDTO mapToAppointmentResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patient(mapPatientResponseDTO(appointment))
                .doctor(mapDoctorResponseDTO(appointment))
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .build();
    }

    private PatientResponseDTO mapPatientResponseDTO(Appointment appointment) {
        var patient = appointment.getPatient();
        var addressDTO = patient.getAddress() != null ?
            PatientResponseDTO.AddressDTO.builder()
                .street(patient.getAddress().getStreet())
                .city(patient.getAddress().getCity())
                .region(patient.getAddress().getRegion())
                .build() : null;

        return PatientResponseDTO.builder()
                .id(patient.getId())
                .fullNameEnglish(patient.getFullNameEnglish())
                .fullNameArabic(patient.getFullNameArabic())
                .email(patient.getEmail())
                .mobileNumber(patient.getMobileNumber())
                .dateOfBirth(patient.getDateOfBirth())
                .nationalId(patient.getNationalId())
                .address(addressDTO)
                .build();
    }

    private DoctorResponseDTO mapDoctorResponseDTO(Appointment appointment) {
        var doctor = appointment.getDoctor();
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .nameEnglish(doctor.getNameEnglish())
                .nameArabic(doctor.getNameArabic())
                .specialty(doctor.getSpecialty())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationDuration(doctor.getConsultationDuration())
                .build();
    }
}