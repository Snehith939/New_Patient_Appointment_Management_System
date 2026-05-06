package com.example.patientapp.controller;

import com.example.patientapp.dto.PrescriptionRequest;
import com.example.patientapp.dto.PrescriptionResponse;
import com.example.patientapp.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prescription endpoints.
 *
 *   POST   /api/prescriptions                        doctor creates a prescription
 *   GET    /api/prescriptions/patient/{patientId}    patient views their prescriptions
 *   PUT    /api/prescriptions/{id}                   doctor updates a prescription
 *   DELETE /api/prescriptions/{id}                   doctor deletes a prescription
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * POST /api/prescriptions
     * Body: { "patientId":1, "doctorId":2, "medicineName":"Paracetamol",
     *         "dose":"500mg", "numberOfDays":"5", "instructions":"Take after meals" }
     *
     * prescribedDate is set automatically to today.
     * Returns 201 with the saved prescription.
     */
    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @RequestBody PrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.createPrescription(request));
    }

    /**
     * GET /api/prescriptions/patient/{patientId}
     * Patient retrieves all their prescriptions across all doctors and dates.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPatientPrescriptions(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(patientId));
    }

    /**
     * PUT /api/prescriptions/{id}
     * Body: any subset of { "medicineName", "dose", "numberOfDays", "instructions" }
     *
     * Only the fields present in the body are updated; omitted fields stay unchanged.
     * patientId and doctorId cannot be changed after creation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, request));
    }

    /**
     * DELETE /api/prescriptions/{id}
     * Doctor removes a prescription record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok("Prescription " + id + " has been deleted.");
    }
}