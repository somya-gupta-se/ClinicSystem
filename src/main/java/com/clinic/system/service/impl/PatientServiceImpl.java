package com.clinic.system.service.impl;

import com.clinic.system.dto.request.PatientRequestDTO;
import com.clinic.system.entity.Address;
import com.clinic.system.entity.Patient;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.PatientRepository;
import com.clinic.system.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;



    @Override
    public Patient registerPatient(PatientRequestDTO dto) {

        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (repository.existsByNationalId(dto.getNationalId())) {
            throw new DuplicateResourceException("National ID already exists");
        }

        Patient patient = Patient.builder()
                .fullNameEnglish(dto.getFullNameEnglish())
                .fullNameArabic(dto.getFullNameArabic())
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .nationalId(dto.getNationalId())
                .createdAt(LocalDateTime.now())
                .address(mapAddress(dto))
                .build();

        return repository.save(patient);
    }

    private Address mapAddress(PatientRequestDTO dto) {
        if (dto.getAddress() == null) {
            return null;
        }
        Address address = new Address();
        address.setStreet(dto.getAddress().getStreet());
        address.setCity(dto.getAddress().getCity());
        address.setRegion(dto.getAddress().getRegion());
        return address;
    }

    @Override
    public List<Patient> getAllPatients() {
        return repository.findAll()
                .stream()
                .filter(p -> !p.isDeleted())
                .toList();
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patient.setDeleted(true);
        repository.save(patient);
    }
}