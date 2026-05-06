package com.example.patientapp.repository;

import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA derives SQL from the method name.
 * Reading the name left-to-right maps directly to WHERE clauses:
 *
 *   findBy                -> SELECT * FROM appointment WHERE
 *   Patient_PatientId     -> patient.patientId = ?
 *   And                   -> AND
 *   AppointmentDate       -> appointmentDate
 *   Before                -> < ?
 *   OrderBy               -> ORDER BY
 *   AppointmentDate       -> appointmentDate
 *   Desc                  -> DESC
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // — Doctor queries ————————————————————————————————————————

    List<Appointment> findByDoctor_DoctorId(Long doctorId);

    // Used for slot availability check (excludes CANCELED appointments)
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    // Today's schedule: all appointments for a doctor on a specific date, ordered by time
    // SQL: SELECT * FROM appointment WHERE doctorId = ? AND appointmentDate = ? ORDER BY timeSlot ASC
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateOrderByTimeSlotAsc(
            Long doctorId, LocalDate date);

    // — Patient queries ———————————————————————————————————————

    // All appointments for a patient (used internally)
    List<Appointment> findByPatient_PatientId(Long patientId);

    /**
     * HISTORY: appointments whose date is strictly before today.
     * Ordered newest-first so the most recent visit appears at the top.
     *
     * SQL equivalent:
     *   SELECT * FROM appointment
     *   WHERE patientId = ?
     *   AND   appointmentDate < ?
     *   ORDER BY appointmentDate DESC
     */
    List<Appointment> findByPatient_PatientIdAndAppointmentDateBeforeOrderByAppointmentDateDesc(
            Long patientId, LocalDate today);

    /**
     * UPCOMING: appointments on today or in the future that are still BOOKED.
     * Ordered soonest-first so the next appointment is at the top.
     *
     * SQL equivalent:
     *   SELECT * FROM appointment
     *   WHERE patientId = ?
     *   AND   appointmentDate >= ?
     *   AND   status = 'BOOKED'
     *   ORDER BY appointmentDate ASC
     */
    List<Appointment> findByPatient_PatientIdAndAppointmentDateGreaterThanEqualAndStatusOrderByAppointmentDateAsc(
            Long patientId, LocalDate today, AppointmentStatus status);

    // — Cascade-delete helpers (used by AdminService) —————————

    // Delete all appointments belonging to a patient before deleting the patient row
    @Transactional
    void deleteByPatient_PatientId(Long patientId);

    // Delete all appointments belonging to a doctor before deleting the doctor row
    @Transactional
    void deleteByDoctor_DoctorId(Long doctorId);
}