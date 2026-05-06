package com.example.patientapp.repository;

import com.example.patientapp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Patient.
 *
 * By extending JpaRepository<Patient, Long> you get these methods for FREE:
 *   save(entity)      -> INSERT or UPDATE
 *   findById(id)      -> SELECT WHERE patientId = ?
 *   findAll()         -> SELECT * FROM patient
 *   deleteById(id)    -> DELETE WHERE patientId = ?
 *   existsById(id)    -> returns boolean
 *   count()           -> SELECT COUNT(*)
 *
 * You can also declare method names and Spring generates the SQL automatically.
 * Example: findByEmail(email) -> SELECT * FROM patient WHERE email = ?
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Spring Data derives: SELECT * FROM patient WHERE email = ?
    Optional<Patient> findByEmail(String email);
}