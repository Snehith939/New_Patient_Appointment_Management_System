package com.example.patientapp.dto;

import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import lombok.Data;
import java.time.LocalTime;

/**
 * What a doctor sees when they check their schedule.
 * Shows patient details + appointment info — mirrors AppointmentResponse
 * but from the doctor's perspective (patient info instead of doctor info).
 */
@Data
public class DoctorScheduleResponse {

    private Long              appointmentId;
    private Long              patientId;
    private String            patientName;
    private String            patientPhone;
    private LocalTime         timeSlot;
    private AppointmentStatus status;

    public static DoctorScheduleResponse from(Appointment appointment) {
        DoctorScheduleResponse res = new DoctorScheduleResponse();
        res.setAppointmentId(appointment.getAppointmentId());
        res.setPatientId(appointment.getPatient().getPatientId());
        res.setPatientName(appointment.getPatient().getName());
        res.setPatientPhone(appointment.getPatient().getPhone());
        res.setTimeSlot(appointment.getTimeSlot());
        res.setStatus(appointment.getStatus());
        return res;
    }
}