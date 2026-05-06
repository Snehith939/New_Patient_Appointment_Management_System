package com.example.patientapp.service;

import com.example.patientapp.dto.PrescriptionRequest;
import com.example.patientapp.dto.PrescriptionResponse;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.model.Prescription;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import com.example.patientapp.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // — Create —

    /**
     * Doctor issues a new prescription to a patient.
     * prescribedDate is set to today automatically.
     */
    public PrescriptionResponse createPrescription(PrescriptionRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicineName(req.getMedicineName());
        prescription.setDose(req.getDose());
        prescription.setNumberOfDays(req.getNumberOfDays());
        prescription.setInstructions(req.getInstructions());
        prescription.setPrescribedDate(LocalDate.now());

        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    // — Read —

    /**
     * Patient retrieves all their prescriptions (all doctors, all dates).
     */
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        return prescriptionRepository.findByPatient_PatientId(patientId)
                .stream()
                .map(PrescriptionResponse::from)
                .collect(Collectors.toList());
    }

    // — Update —

    /**
     * Doctor updates an existing prescription.
     * Only non-null, non-blank fields in the request overwrite the stored value.
     * patientId and doctorId are NOT re-assignable - a prescription's ownership
     * is fixed at creation time.
     */
    public PrescriptionResponse updatePrescription(Long prescriptionId, PrescriptionRequest req) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));

        if (req.getMedicineName() != null && !req.getMedicineName().isBlank()) {
            prescription.setMedicineName(req.getMedicineName());
        }
        if (req.getDose() != null && !req.getDose().isBlank()) {
            prescription.setDose(req.getDose());
        }
        if (req.getNumberOfDays() != null && !req.getNumberOfDays().isBlank()) {
            prescription.setNumberOfDays(req.getNumberOfDays());
        }
        if (req.getInstructions() != null && !req.getInstructions().isBlank()) {
            prescription.setInstructions(req.getInstructions());
        }

        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    // — Delete —

    public void deletePrescription(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new RuntimeException("Prescription not found: " + prescriptionId);
        }
        prescriptionRepository.deleteById(prescriptionId);
    }
}