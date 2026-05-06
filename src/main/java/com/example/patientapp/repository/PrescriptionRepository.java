package com.example.patientapp.repository;

import com.example.patientapp.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // All prescriptions for a patient - used by patient view
    List<Prescription> findByPatient_PatientId(Long patientId);

    // Cascade-delete helpers used by AdminService
    @Transactional
    void deleteByPatient_PatientId(Long patientId);

    @Transactional
    void deleteByDoctor_DoctorId(Long doctorId);
}