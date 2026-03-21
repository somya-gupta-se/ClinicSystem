package com.clinic.system.service.impl;

import com.clinic.system.dto.request.AppointmentRequestDTO;
import com.clinic.system.entity.Appointment;
import com.clinic.system.entity.Doctor;
import com.clinic.system.entity.Patient;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.AppointmentRepository;
import com.clinic.system.repository.DoctorRepository;
import com.clinic.system.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepo;

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;


    @Test
    void scheduleAppointment_success() {
        // Create a patient and doctor for the test
        Patient patient = Patient.builder().id(1L).isDeleted(false).build();
        Doctor doctor = Doctor.builder().id(2L).build();

        AppointmentRequestDTO dto = new AppointmentRequestDTO(1L, 2L, LocalDate.now(), LocalTime.NOON, "");

        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(false);
        when(appointmentRepo.existsByPatientIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(false);

        Appointment saved = Appointment.builder().id(10L).build();
        when(appointmentRepo.save(any())).thenReturn(saved);

        Appointment result = appointmentService.scheduleAppointment(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(appointmentRepo).save(any());
    }

    @Test
    void scheduleAppointment_patientNotFound() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON
        ,"");

        when(patientRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.scheduleAppointment(dto));
    }

    @Test
    void scheduleAppointment_patientDeleted() {
        Patient patient = Patient.builder().id(1L).isDeleted(true).build();

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON, ""
        );

        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.scheduleAppointment(dto));
    }

    @Test
    void scheduleAppointment_doctorSlotAlreadyBooked() {
        Patient patient = Patient.builder().id(1L).isDeleted(false).build();
        Doctor doctor = Doctor.builder().id(2L).build();

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON, ""
        );

        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> appointmentService.scheduleAppointment(dto));
    }

    @Test
    void scheduleAppointment_patientAlreadyHasAppointment() {
        Patient patient = Patient.builder().id(1L).isDeleted(false).build();
        Doctor doctor = Doctor.builder().id(2L).build();

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON, ""
        );

        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(false);
        when(appointmentRepo.existsByPatientIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> appointmentService.scheduleAppointment(dto));
    }

    // ------------------- updateAppointment -------------------

    @Test
    void updateAppointment_success() {
        Appointment appointment = Appointment.builder().id(1L).build();
        Doctor doctor = Doctor.builder().id(2L).build();

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON, ""
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(any(), any(), any()))
                .thenReturn(false);

        when(appointmentRepo.save(any())).thenReturn(appointment);

        Appointment result = appointmentService.updateAppointment(1L, dto);

        assertNotNull(result);
        verify(appointmentRepo).save(appointment);
    }

    @Test
    void updateAppointment_notFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                1L, 2L, LocalDate.now(), LocalTime.NOON, ""
        );

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.updateAppointment(1L, dto));
    }

    // ------------------- getAllAppointments -------------------

    @Test
    void getAllAppointments_success() {
        when(appointmentRepo.findAllWithPatientAndDoctor())
                .thenReturn(List.of(new Appointment()));

        List<Appointment> result = appointmentService.getAllAppointments();

        assertFalse(result.isEmpty());
    }

    // ------------------- getAppointmentById -------------------

    @Test
    void getAppointmentById_success() {
        Appointment appointment = new Appointment();

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentById(1L);

        assertNotNull(result);
    }

    @Test
    void getAppointmentById_notFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(1L));
    }

    // ------------------- deleteAppointment -------------------

    @Test
    void deleteAppointment_success() {
        Appointment appointment = new Appointment();

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepo).delete(appointment);
    }

    @Test
    void deleteAppointment_notFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.deleteAppointment(1L));
    }
}