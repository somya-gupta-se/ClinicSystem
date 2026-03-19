package com.clinic.system.service.impl;

import com.clinic.system.dto.request.AppointmentRequestDTO;
import com.clinic.system.entity.Appointment;
import com.clinic.system.entity.Doctor;
import com.clinic.system.entity.Patient;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.*;
import com.clinic.system.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;


    @Override
    public Appointment scheduleAppointment(AppointmentRequestDTO dto) {

        Patient patient = patientRepo.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Doctor doctor = doctorRepo.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(), dto.appointmentDate(), dto.appointmentTime())) {
            throw new DuplicateResourceException("Slot already booked for this doctor at the specified time");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(dto.appointmentDate())
                .appointmentTime(dto.appointmentTime())
                .status("SCHEDULED")
                .build();

        return appointmentRepo.save(appointment);
    }

    @Override
    public Appointment updateAppointment(Long id, AppointmentRequestDTO dto) {

        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Doctor doctor = doctorRepo.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(), dto.appointmentDate(), dto.appointmentTime())) {
            throw new DuplicateResourceException("Slot already booked for this doctor at the specified time");
        }

        appointment.setAppointmentTime(dto.appointmentTime());
        appointment.setDoctor(doctor);

        return appointmentRepo.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointmentRepo.delete(appointment);
    }
}