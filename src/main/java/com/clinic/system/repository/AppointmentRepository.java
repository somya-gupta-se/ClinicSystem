package com.clinic.system.repository;

import com.clinic.system.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(Long doctorId, LocalDate date, LocalTime time);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor")
    List<Appointment> findAllWithPatientAndDoctor();

    // Find all appointments for a patient
    List<Appointment> findByPatientId(Long patientId);

    // Delete all appointments for a patient
    void deleteByPatientId(Long patientId);

    boolean existsByPatientIdAndAppointmentDateAndAppointmentTime(Long doctorId, LocalDate date, LocalTime time);
}