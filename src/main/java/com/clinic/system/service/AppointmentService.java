package com.clinic.system.service;

import com.clinic.system.dto.request.AppointmentRequestDTO;
import com.clinic.system.entity.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment scheduleAppointment(AppointmentRequestDTO dto);

    Appointment updateAppointment(Long id, AppointmentRequestDTO dto);

    List<Appointment> getAllAppointments();

    Appointment getAppointmentById(Long id);

    void deleteAppointment(Long id);
}