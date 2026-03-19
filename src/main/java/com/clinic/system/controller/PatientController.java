package com.clinic.system.controller;

import com.clinic.system.dto.request.PatientRequestDTO;
import com.clinic.system.dto.response.ApiResponseDTO;
import com.clinic.system.dto.response.PatientResponseDTO;
import com.clinic.system.entity.Patient;
import com.clinic.system.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<PatientResponseDTO>> register(@Valid @RequestBody PatientRequestDTO dto) {
        Patient patient = service.registerPatient(dto);
        PatientResponseDTO response = mapToPatientResponseDTO(patient);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, "Patient registered successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PatientResponseDTO>>> getAll() {
        List<Patient> patients = service.getAllPatients();
        List<PatientResponseDTO> response = patients.stream()
                .map(this::mapToPatientResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Patients retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.deletePatient(id);

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Patient deleted successfully"));
    }

    private PatientResponseDTO mapToPatientResponseDTO(Patient patient) {
        PatientResponseDTO.AddressDTO addressDTO = null;
        if (patient.getAddress() != null) {
            addressDTO = PatientResponseDTO.AddressDTO.builder()
                    .street(patient.getAddress().getStreet())
                    .city(patient.getAddress().getCity())
                    .region(patient.getAddress().getRegion())
                    .build();
        }

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
}