package com.example.patientapp.service;

import com.example.patientapp.dto.AppointmentResponse;
import com.example.patientapp.dto.UpdatePatientRequest;
import com.example.patientapp.model.AppointmentStatus;
import com.example.patientapp.model.Patient;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // — Update personal details —

    /**
     * Updates only the four personal fields.
     * Email, password, and role cannot be changed here.
     *
     * Uses UpdatePatientRequest DTO so the controller can never accidentally
     * pass email or password - those fields don't exist on the DTO.
     */
    public Patient updatePatientProfile(Long patientId, UpdatePatientRequest req) {
        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        existing.setName(req.getName());
        existing.setPhone(req.getPhone());
        existing.setAddress(req.getAddress());
        existing.setDob(req.getDob());

        return patientRepository.save(existing);
    }

    // — Appointment history —

    /**
     * Returns all past appointments (appointmentDate < today), newest first.
     * Includes BOOKED (patient no-showed), COMPLETED, and CANCELED records -
     * anything in the past is part of the history.
     *
     * Each Appointment entity is converted to a flat AppointmentResponse DTO
     * so the caller gets exactly: doctorName, specialization, date, timeSlot, status.
     */
    public List<AppointmentResponse> getAppointmentHistory(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        return appointmentRepository
                .findByPatient_PatientIdAndAppointmentDateBeforeOrderByAppointmentDateDesc(
                        patientId, LocalDate.now())
                .stream()
                .map(AppointmentResponse::from)  // entity -> DTO
                .collect(Collectors.toList());
    }

    // — Upcoming appointments —

    /**
     * Returns all future appointments with status BOOKED, soonest first.
     * CANCELED appointments are excluded - if a patient cancels, it no longer
     * appears in their upcoming list.
     */
    public List<AppointmentResponse> getUpcomingAppointments(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        return appointmentRepository
                .findByPatient_PatientIdAndAppointmentDateGreaterThanEqualAndStatusOrderByAppointmentDateAsc(
                        patientId, LocalDate.now(), AppointmentStatus.BOOKED)
                .stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }
}