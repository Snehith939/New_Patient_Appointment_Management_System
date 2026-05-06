package com.example.patientapp.service;

import com.example.patientapp.dto.UpdateAdminRequest;
import com.example.patientapp.model.Admin;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.repository.AdminRepository;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.BlockedSlotRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import com.example.patientapp.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final BlockedSlotRepository blockedSlotRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AdminService(AdminRepository adminRepository,
                        PatientRepository patientRepository,
                        DoctorRepository doctorRepository,
                        AppointmentRepository appointmentRepository,
                        BlockedSlotRepository blockedSlotRepository,
                        PrescriptionRepository prescriptionRepository) {
        this.adminRepository = adminRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.blockedSlotRepository = blockedSlotRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    // — Patient management —

    public List<Patient> getAllPatients() { return patientRepository.findAll(); }

    /**
     * Delete a patient and all of their appointments.
     *
     * Order matters:
     *  1. Delete appointments (FK child rows) first.
     *  2. Then delete the patient (FK parent row).
     *
     * Both deletes run inside a single transaction so the DB is never left
     * in a half-deleted state if something goes wrong mid-way.
     */
    @Transactional
    public void deletePatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        prescriptionRepository.deleteByPatient_PatientId(patientId);
        appointmentRepository.deleteByPatient_PatientId(patientId);
        patientRepository.deleteById(patientId);
    }

    // — Doctor management —

    public List<Doctor> getAllDoctors() { return doctorRepository.findAll(); }

    /**
     * Delete a doctor along with all their appointments and blocked slots.
     *
     * Order:
     *  1. Delete blocked_slot rows (references doctor).
     *  2. Delete appointment rows (references doctor).
     *  3. Delete the doctor row.
     */
    @Transactional
    public void deleteDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found: " + doctorId);
        }
        prescriptionRepository.deleteByDoctor_DoctorId(doctorId);
        blockedSlotRepository.deleteByDoctor_DoctorId(doctorId);
        appointmentRepository.deleteByDoctor_DoctorId(doctorId);
        doctorRepository.deleteById(doctorId);
    }

    // — Admin self-management —

    public List<Admin> getAllAdmins() { return adminRepository.findAll(); }

    /**
     * Update an admin's own name and/or job title (role).
     * Email and password are not updatable here.
     */
    public Admin updateAdmin(Long adminId, UpdateAdminRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        if (request.getName() != null && !request.getName().isBlank()) {
            admin.setName(request.getName());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            admin.setRole(request.getRole());
        }
        return adminRepository.save(admin);
    }

    /**
     * An admin can delete their own account.
     * No dependent rows reference the admin table, so a plain delete is safe.
     */
    public void deleteAdmin(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new RuntimeException("Admin not found: " + adminId);
        }
        adminRepository.deleteById(adminId);
    }
}