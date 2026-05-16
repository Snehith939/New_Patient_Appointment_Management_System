package com.example.patientapp.repository;
import com.example.patientapp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // SELECT * FROM payment WHERE appointmentId = ?
    Optional<Payment> findByAppointment_AppointmentId(Long appointmentId);

    // All payments for a patient (via appointment -> patient), newest first
    // SQL: SELECT p.* FROM payment p
    //      JOIN appointment a ON p.appointmentId = a.appointmentId
    //      WHERE a.patientId = ?
    //      ORDER BY p.createdAt DESC
    List<Payment> findByAppointment_Patient_PatientIdOrderByCreatedAtDesc(Long patientId);
}
