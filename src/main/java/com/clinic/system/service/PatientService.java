package com.clinic.system.service;

import com.clinic.system.dto.request.PatientRequestDTO;
import com.clinic.system.entity.Patient;

import java.util.List;

public interface PatientService {

    Patient registerPatient(PatientRequestDTO dto);

    List<Patient> getAllPatients();

    void deletePatient(Long id);
}