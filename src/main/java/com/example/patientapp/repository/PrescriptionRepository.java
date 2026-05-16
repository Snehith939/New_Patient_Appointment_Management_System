package com.example.patientapp.repository;

import java.util.List;
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.patientapp.model.Prescription;
 
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
 
    // ✅ Get all prescriptions of a patient
    List<Prescription> findByPatientPatientId(Long patientId);
 
    // ✅ Used for secure update/delete (doctor validation)
    Optional<Prescription> findByPrescriptionIdAndDoctorDoctorId(Long id, Long doctorId);
 
    // ✅ ✅ NEW: check if prescription already exists for appointment
    boolean existsByAppointmentAppointmentId(Long appointmentId);
}