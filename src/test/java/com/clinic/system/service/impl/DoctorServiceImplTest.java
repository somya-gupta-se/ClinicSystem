package com.clinic.system.service.impl;

import com.clinic.system.dto.request.DoctorRequestDTO;
import com.clinic.system.entity.Doctor;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository repository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    // ================= CREATE =================

    @Test
    void createDoctor_success() {
        DoctorRequestDTO dto = new DoctorRequestDTO(
                "Dr John",
                "دكتور جون",
                "Cardiology",
                10,
                30
        );

        Doctor savedDoctor = Doctor.builder()
                .id(1L)
                .nameEnglish("Dr John")
                .build();

        when(repository.save(any(Doctor.class))).thenReturn(savedDoctor);

        Doctor result = doctorService.createDoctor(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(any(Doctor.class));
    }

    // ================= GET BY ID =================

    @Test
    void getDoctorById_success() {
        Doctor doctor = Doctor.builder().id(1L).nameEnglish("Dr John").build();

        when(repository.findById(1L)).thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getDoctorById(1L);

        assertNotNull(result);
        assertEquals("Dr John", result.getNameEnglish());
    }

    @Test
    void getDoctorById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.getDoctorById(1L));
    }

    // ================= GET ALL =================

    @Test
    void getAllDoctors_success() {
        List<Doctor> doctors = List.of(
                Doctor.builder().id(1L).build(),
                Doctor.builder().id(2L).build()
        );

        when(repository.findAll()).thenReturn(doctors);

        List<Doctor> result = doctorService.getAllDoctors();

        assertEquals(2, result.size());
    }

    // ================= GET BY SPECIALTY =================

    @Test
    void getDoctorsBySpecialty_success() {
        List<Doctor> doctors = List.of(
                Doctor.builder().specialty("Cardiology").build()
        );

        when(repository.findBySpecialty("Cardiology")).thenReturn(doctors);

        List<Doctor> result = doctorService.getDoctorsBySpecialty("Cardiology");

        assertEquals(1, result.size());
        verify(repository).findBySpecialty("Cardiology");
    }

    // ================= UPDATE =================

    @Test
    void updateDoctor_success() {
        Doctor existingDoctor = Doctor.builder()
                .id(1L)
                .nameEnglish("Old Name")
                .build();

        DoctorRequestDTO dto = new DoctorRequestDTO(
                "New Name",
                "Arabic",
                "Dermatology",
                5,
                20
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingDoctor));
        when(repository.save(any(Doctor.class))).thenReturn(existingDoctor);

        Doctor result = doctorService.updateDoctor(1L, dto);

        assertEquals("New Name", result.getNameEnglish());
        verify(repository).save(existingDoctor);
    }

    @Test
    void updateDoctor_notFound() {
        DoctorRequestDTO dto = new DoctorRequestDTO(
                "New Name", "Arabic", "Dermatology", 5, 20
        );

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.updateDoctor(1L, dto));
    }

    // ================= DELETE =================

    @Test
    void deleteDoctor_success() {
        Doctor doctor = Doctor.builder().id(1L).build();

        when(repository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.deleteDoctor(1L);

        verify(repository).delete(doctor);
    }

    @Test
    void deleteDoctor_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.deleteDoctor(1L));
    }
}