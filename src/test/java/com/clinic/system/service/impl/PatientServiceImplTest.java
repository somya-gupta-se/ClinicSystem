package com.clinic.system.service.impl;

import com.clinic.system.async.NotificationService;
import com.clinic.system.dto.request.PatientRequestDTO;
import com.clinic.system.entity.Address;
import com.clinic.system.entity.Patient;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.AppointmentRepository;
import com.clinic.system.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    // ================= REGISTER =================

    @Test
    void registerPatient_success() {
        PatientRequestDTO dto = new PatientRequestDTO(
                "John Doe",
                "جون",
                "john@mail.com",
                "9999999999",
                LocalDate.of(1990, 1, 1),
                "NID123",
                new PatientRequestDTO.AddressDTO("Street", "City", "Region")
        );

        when(repository.existsByEmail("john@mail.com")).thenReturn(false);
        when(repository.existsByNationalId("NID123")).thenReturn(false);

        Patient savedPatient = Patient.builder().id(1L).build();
        when(repository.save(any(Patient.class))).thenReturn(savedPatient);

        Patient result = patientService.registerPatient(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(notificationService)
                .sendPatientRegistrationNotification("John Doe");
        verify(repository).save(any(Patient.class));
    }

    @Test
    void registerPatient_duplicateEmail() {
        PatientRequestDTO dto = mock(PatientRequestDTO.class);

        when(dto.email()).thenReturn("test@mail.com");
        when(repository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.registerPatient(dto));
    }

    @Test
    void registerPatient_duplicateNationalId() {
        PatientRequestDTO dto = mock(PatientRequestDTO.class);

        when(dto.email()).thenReturn("test@mail.com");
        when(dto.nationalId()).thenReturn("NID123");

        when(repository.existsByEmail("test@mail.com")).thenReturn(false);
        when(repository.existsByNationalId("NID123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.registerPatient(dto));
    }

    @Test
    void registerPatient_withoutAddress() {
        PatientRequestDTO dto = new PatientRequestDTO(
                "John",
                "جون",
                "john@mail.com",
                "9999999999",
                LocalDate.of(1990, 1, 1),
                "NID123",
                null
        );

        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.existsByNationalId(any())).thenReturn(false);

        when(repository.save(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Patient result = patientService.registerPatient(dto);

        assertNull(result.getAddress());
    }

    // ================= GET ALL =================

    @Test
    void getAllPatients_filtersDeleted() {
        Patient p1 = Patient.builder().id(1L).isDeleted(false).build();
        Patient p2 = Patient.builder().id(2L).isDeleted(true).build();

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<Patient> result = patientService.getAllPatients();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isDeleted());
    }

    // ================= DELETE =================

    @Test
    void deletePatient_success() {
        Patient patient = Patient.builder().id(1L).isDeleted(false).build();

        when(repository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.deletePatient(1L);

        // soft delete check
        assertTrue(patient.isDeleted());

        verify(repository).save(patient);
        verify(appointmentRepository).deleteByPatientId(1L);
    }

    @Test
    void deletePatient_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.deletePatient(1L));
    }
}