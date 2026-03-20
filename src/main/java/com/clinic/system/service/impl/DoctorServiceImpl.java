package com.clinic.system.service.impl;

import com.clinic.system.dto.request.DoctorRequestDTO;
import com.clinic.system.entity.Doctor;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.DoctorRepository;
import com.clinic.system.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository repository;

    @Override
    public Doctor createDoctor(DoctorRequestDTO dto) {
        Doctor doctor = Doctor.builder()
                .nameEnglish(dto.nameEnglish())
                .nameArabic(dto.nameArabic())
                .specialty(dto.specialty())
                .yearsOfExperience(dto.yearsOfExperience())
                .consultationDuration(dto.consultationDuration())
                .build();

        return repository.save(doctor);
    }

    @Cacheable(value = "doctors", key = "#id")
    @Override
    public Doctor getDoctorById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    @Cacheable(value = "doctors")
    @Override
    public List<Doctor> getAllDoctors() {
        log.info("Fetching doctors from DB...");
        return repository.findAll();
    }

    @Override
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return repository.findBySpecialty(specialty);
    }

    @CacheEvict(value = "doctors", key = "#id")
    @Override
    public Doctor updateDoctor(Long id, DoctorRequestDTO dto) {
        Doctor doctor = getDoctorById(id);

        doctor.setNameEnglish(dto.nameEnglish());
        doctor.setNameArabic(dto.nameArabic());
        doctor.setSpecialty(dto.specialty());
        doctor.setYearsOfExperience(dto.yearsOfExperience());
        doctor.setConsultationDuration(dto.consultationDuration());

        return repository.save(doctor);
    }

    @CacheEvict(value = "doctors", key = "#id")
    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        repository.delete(doctor);
    }
}

