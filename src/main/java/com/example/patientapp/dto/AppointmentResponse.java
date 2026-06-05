package com.example.patientapp.dto;

import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A flat response DTO for appointment details.
 * The static from() factory method converts an Appointment entity -> this DTO.
 */
@Data
public class AppointmentResponse {

    private Long appointmentId;

    // Doctor details the patient cares about
    private Long   doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorPhone;

    // Appointment details
    private LocalDate        appointmentDate;
    private LocalTime        timeSlot;
    private AppointmentStatus status;
  

    private Long   patientId;
    private String patientName;

    /**
     * Converts an Appointment entity to this DTO.
     * Called in PatientService — never in the controller.
     */
    public static AppointmentResponse from(Appointment appointment) {
        AppointmentResponse res = new AppointmentResponse();
        res.setAppointmentId(appointment.getAppointmentId());

        res.setDoctorId(appointment.getDoctor().getDoctorId());
        res.setDoctorName(appointment.getDoctor().getName());
        res.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
        res.setDoctorPhone(appointment.getDoctor().getPhone());

        res.setAppointmentDate(appointment.getAppointmentDate());
        res.setTimeSlot(appointment.getTimeSlot());
        res.setStatus(appointment.getStatus());
        

        res.setPatientId(appointment.getPatient().getPatientId());
        res.setPatientName(appointment.getPatient().getName());


        return res;
    }
}