package com.example.patientapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.patientapp.exception.BadRequestException;
import com.example.patientapp.exception.ResourceNotFoundException;
import com.example.patientapp.exception.UnauthorizedActionException;
import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;       // ← ADD THIS IMPORT
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.model.Prescription;
import com.example.patientapp.model.PrescriptionItem;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import com.example.patientapp.repository.PrescriptionRepository;


import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // ✅ ADD PRESCRIPTION
    public Prescription addPrescription(Prescription prescription,
                                        Long doctorId,
                                        Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (appointment.getDoctor() == null) {
            throw new BadRequestException("No doctor assigned to this appointment");
        }

        if (!appointment.getDoctor().getDoctorId().equals(doctorId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to create this prescription");
        }

        if (prescriptionRepository.existsByAppointmentAppointmentId(appointmentId)) {
            throw new BadRequestException(
                    "Prescription already exists for this appointment");
        }

        // ✅ Only BOOKED appointments can have prescriptions
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BadRequestException(
                    "Cannot add prescription for appointment with status: " 
                    + appointment.getStatus());
        }

        Patient patient = appointment.getPatient();

        prescription.setAppointment(appointment);
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);

        if (prescription.getPrescribedDate() == null) {
            prescription.setPrescribedDate(LocalDate.now());
        }

        if (prescription.getItems() != null) {
            prescription.getItems().forEach(item -> item.setPrescription(prescription));
        }

        // ✅ Save prescription first
        Prescription saved = prescriptionRepository.save(prescription);

        // ✅ AUTO-COMPLETE: Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return saved;
    }

    // ✅ GET ALL
    public List<Prescription> getAllPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientPatientId(patientId);
    }

    // ✅ GET ONE
    public Prescription getPrescription(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found"));
    }

    // ✅ DELETE
    public void deletePrescription(Long prescriptionId, Long doctorId) {

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found"));

        if (!prescription.getDoctor().getDoctorId().equals(doctorId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this prescription");
        }

        prescriptionRepository.delete(prescription);
    }

    // ✅ UPDATE
    public Prescription updatePrescription(Long prescriptionId,
                                           Long doctorId,
                                           Prescription updatedPrescription) {

        Prescription existing = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found"));

        if (!existing.getDoctor().getDoctorId().equals(doctorId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to update this prescription");
        }

        if (updatedPrescription.getPrescribedDate() != null) {
            existing.setPrescribedDate(updatedPrescription.getPrescribedDate());
        }

        if (updatedPrescription.getItems() != null) {

            existing.getItems().removeIf(existingItem ->
                updatedPrescription.getItems().stream()
                    .noneMatch(updatedItem ->
                            updatedItem.getId() != null &&
                            updatedItem.getId().equals(existingItem.getId()))
            );

            for (PrescriptionItem updatedItem : updatedPrescription.getItems()) {

                if (updatedItem.getId() != null) {

                    PrescriptionItem existingItem = existing.getItems().stream()
                            .filter(item -> item.getId().equals(updatedItem.getId()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Item not found with ID: " 
                                            + updatedItem.getId()));

                    if (updatedItem.getMedicineName() != null &&
                            !updatedItem.getMedicineName().trim().isEmpty()) {
                        existingItem.setMedicineName(
                                updatedItem.getMedicineName().trim());
                    }

                    if (updatedItem.getInstructions() != null &&
                            !updatedItem.getInstructions().trim().isEmpty()) {
                        existingItem.setInstructions(
                                updatedItem.getInstructions().trim());
                    }

                    if (updatedItem.getNumberOfDays() != null) {
                        if (updatedItem.getNumberOfDays() <= 0) {
                            throw new BadRequestException(
                                    "numberOfDays must be greater than 0");
                        }
                        existingItem.setNumberOfDays(updatedItem.getNumberOfDays());
                    }

                    if (updatedItem.getMorning() != null) {
                        existingItem.setMorning(updatedItem.getMorning());
                    }
                    if (updatedItem.getAfternoon() != null) {
                        existingItem.setAfternoon(updatedItem.getAfternoon());
                    }
                    if (updatedItem.getNight() != null) {
                        existingItem.setNight(updatedItem.getNight());
                    }

                } else {

                    if (updatedItem.getMedicineName() == null ||
                        updatedItem.getMedicineName().trim().isEmpty()) {
                        throw new BadRequestException(
                                "Medicine name is required for new item");
                    }

                    if (updatedItem.getNumberOfDays() == null ||
                        updatedItem.getNumberOfDays() <= 0) {
                        throw new BadRequestException(
                                "Valid numberOfDays required");
                    }

                    updatedItem.setMedicineName(
                            updatedItem.getMedicineName().trim());
                    updatedItem.setPrescription(existing);
                    existing.getItems().add(updatedItem);
                }
            }
        }

        return prescriptionRepository.save(existing);
    }
}