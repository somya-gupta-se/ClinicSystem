package com.clinic.system.controller;

import com.clinic.system.dto.request.DoctorRequestDTO;
import com.clinic.system.dto.response.ApiResponseDTO;
import com.clinic.system.dto.response.DoctorResponseDTO;
import com.clinic.system.entity.Doctor;
import com.clinic.system.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService service;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DoctorResponseDTO>>> getAllDoctors() {
        List<Doctor> doctors = service.getAllDoctors();
        List<DoctorResponseDTO> response = doctors.stream()
                .map(this::mapToDoctorResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Doctors retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DoctorResponseDTO>> getDoctorById(@PathVariable Long id) {
        Doctor doctor = service.getDoctorById(id);
        DoctorResponseDTO response = mapToDoctorResponseDTO(doctor);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Doctor retrieved successfully"));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<ApiResponseDTO<List<DoctorResponseDTO>>> getDoctorsBySpecialty(@PathVariable String specialty) {
        List<Doctor> doctors = service.getDoctorsBySpecialty(specialty);
        List<DoctorResponseDTO> response = doctors.stream()
                .map(this::mapToDoctorResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Doctors retrieved successfully by specialty"));
    }


    @PostMapping
    public ResponseEntity<ApiResponseDTO<DoctorResponseDTO>> createDoctor(@Valid @RequestBody DoctorRequestDTO dto) {
        Doctor doctor = service.createDoctor(dto);
        DoctorResponseDTO response = mapToDoctorResponseDTO(doctor);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, "Doctor created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DoctorResponseDTO>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO dto) {
        Doctor doctor = service.updateDoctor(id, dto);
        DoctorResponseDTO response = mapToDoctorResponseDTO(doctor);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Doctor updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDoctor(@PathVariable Long id) {
        service.deleteDoctor(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Doctor deleted successfully"));
    }

    private DoctorResponseDTO mapToDoctorResponseDTO(Doctor doctor) {
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
