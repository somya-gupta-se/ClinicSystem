package com.clinic.system.service.impl;

import com.clinic.system.async.NotificationService;
import com.clinic.system.dto.request.PatientRequestDTO;
import com.clinic.system.entity.Address;
import com.clinic.system.entity.Patient;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.AppointmentRepository;
import com.clinic.system.repository.PatientRepository;
import com.clinic.system.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;
    private final NotificationService notificationService;
    private final AppointmentRepository appointmentRepository;


    @Override
    public Patient registerPatient(PatientRequestDTO dto) {

        if (repository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (repository.existsByNationalId(dto.nationalId())) {
            throw new DuplicateResourceException("National ID already exists");
        }

        Patient patient = Patient.builder()
                .fullNameEnglish(dto.fullNameEnglish())
                .fullNameArabic(dto.fullNameArabic())
                .email(dto.email())
                .mobileNumber(dto.mobileNumber())
                .dateOfBirth(dto.dateOfBirth())
                .nationalId(dto.nationalId())
                .createdAt(LocalDateTime.now())
                .address(mapAddress(dto))
                .build();
        // Async call (non-blocking)
        notificationService.sendPatientRegistrationNotification(dto.fullNameEnglish());
        return repository.save(patient);
    }

    private Address mapAddress(PatientRequestDTO dto) {
        if (dto.address() == null) {
            return null;
        }
        Address address = new Address();
        address.setStreet(dto.address().street());
        address.setCity(dto.address().city());
        address.setRegion(dto.address().region());
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
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Soft delete patient
        patient.setDeleted(true);
        repository.save(patient);

        // Hard delete all appointments for this patient (thus releasing slots)
        appointmentRepository.deleteByPatientId(id);
    }
}