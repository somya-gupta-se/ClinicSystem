package com.clinic.system.service;

import com.clinic.system.dto.request.DoctorRequestDTO;
import com.clinic.system.entity.Doctor;

import java.util.List;

public interface DoctorService {

    Doctor createDoctor(DoctorRequestDTO dto);

    Doctor getDoctorById(Long id);

    List<Doctor> getAllDoctors();

    List<Doctor> getDoctorsBySpecialty(String specialty);

    Doctor updateDoctor(Long id, DoctorRequestDTO dto);

    void deleteDoctor(Long id);
}

