package com.example.patientapp.repository;

import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // —  Use by Doctor queries
	
    // - Doctor's all appointments
    List<Appointment> findByDoctor_DoctorId(Long doctorId);

    // Used for slot availability check (excludes CANCELED appointments)
    // Used in → getAvailableSlots() method
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    // - Doctor's daily schedule
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateOrderByTimeSlotAsc(
            Long doctorId, LocalDate date);
    
    

    // Use by Patient queries 

    // All appointments for a patient (used internally)
    List<Appointment> findByPatient_PatientId(Long patientId);

    /**
     * HISTORY: appointments whose date is strictly before today.
     */
    List<Appointment> findByPatient_PatientIdAndAppointmentDateBeforeOrderByAppointmentDateDesc(
            Long patientId, LocalDate today);

    /**
     * UPCOMING: appointments on today or in the future that are still BOOKED.
     */
    List<Appointment> findByPatient_PatientIdAndAppointmentDateGreaterThanEqualAndStatusOrderByAppointmentDateAsc(
            Long patientId, LocalDate today, AppointmentStatus status);
    
    
    

    // used by AdminService

    // Delete all appointments belonging to a patient before deleting the patient row
    @Transactional
    void deleteByPatient_PatientId(Long patientId);

    // Delete all appointments belonging to a doctor before deleting the doctor row
    @Transactional
    void deleteByDoctor_DoctorId(Long doctorId);
    
    
    boolean existsByDoctor_DoctorIdAndAppointmentDateAndTimeSlot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime timeSlot);
    
}