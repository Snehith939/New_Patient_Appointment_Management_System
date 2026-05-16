package com.example.patientapp.service;

import com.example.patientapp.dto.RegisterRequest;
import com.example.patientapp.dto.UpdateAdminRequest;
import com.example.patientapp.model.Admin;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.model.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import com.example.patientapp.exception.*;
import com.example.patientapp.repository.AdminRepository;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.BlockedSlotRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.LogsRepository;
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
    private final LogsRepository logsRepository;

    public AdminService(AdminRepository adminRepository,
                        PatientRepository patientRepository,
                        DoctorRepository doctorRepository,
                        AppointmentRepository appointmentRepository,
                        BlockedSlotRepository blockedSlotRepository,
                        PrescriptionRepository prescriptionRepository,
                        LogsRepository logsRepository) {
        this.adminRepository = adminRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.blockedSlotRepository = blockedSlotRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.logsRepository=logsRepository;
    }

    
    //logs
    
    public List<Logs> getAllLogs(){
    	return logsRepository.findAll();
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
    public void deletePatient(Long patientId,String adminEmail) {

    	if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found: " + patientId);
        }

        //prescriptionRepository.deleteByPatient_PatientId(patientId);
        appointmentRepository.deleteByPatient_PatientId(patientId);
        patientRepository.deleteById(patientId);
        logsRepository.save(
	            new Logs(
	                    "DELETE_PATIENT",
	                    adminEmail,
	                    "PATIENT",
	                    patientId,
	                    LocalDateTime.now()
	            )
	);
    }

    // — Doctor management —

    public List<Doctor> getAllDoctors() { return doctorRepository.findAll(); }
    
    
    
    //this part has to implement after intrime
    
//    public Doctor createDoctor(RegisterRequest req) {
//        Doctor d = new Doctor();
//        d.setName(req.getName());
//        d.setEmail(req.getEmail());
//        d.setPassword(req.getPassword());
//        d.setPhone(req.getPhone());
//        d.setSpecialization(req.getSpecialization());
//
//        // Build the JSON string that goes into the availability column
//        if (req.getStartTime() != null && req.getEndTime() != null) {
//            d.setAvailability(buildAvailabilityJson(req.getStartTime(), req.getEndTime()));
//        }
//        return doctorRepository.save(d);
//    }
    

    /**
     * Delete a doctor along with all their appointments and blocked slots.
     *
     * Order:
     *  1. Delete blocked_slot rows (references doctor).
     *  2. Delete appointment rows (references doctor).
     *  3. Delete the doctor row.
     */
    @Transactional
    public void deleteDoctor(Long doctorId, String adminEmail) {

    	if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found: " + doctorId);
        }

        //prescriptionRepository.deleteByDoctor_DoctorId(doctorId);
        blockedSlotRepository.deleteByDoctor_DoctorId(doctorId);
        appointmentRepository.deleteByDoctor_DoctorId(doctorId);
        doctorRepository.deleteById(doctorId);

		logsRepository.save(
		            new Logs(
		                    "DELETE_DOCTOR",
		                    adminEmail,   // later replace with logged-in user
		                    "Doctor",
		                    doctorId,
		                    LocalDateTime.now()
		            )
		    );

    }

    // — Admin self-management —

    public List<Admin> getAllAdmins() { return adminRepository.findAll(); }
    
  //this part has to implement after intrime
    
//    public Admin createAdmin(RegisterRequest req) {
//        Admin a = new Admin();
//        a.setName(req.getName());
//        a.setEmail(req.getEmail());
//        a.setPassword(req.getPassword());
//        a.setRole(req.getAdminRole());
//        return adminRepository.save(a);
//    }
    

    /**
     * Update an admin's own name and/or job title (role).
     * Email and password are not updatable here.
     */
    public Admin updateAdmin(Long adminId, UpdateAdminRequest request, String adminEmail) {

    	Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found: " + adminId)
                );

        if (request.getName() != null && !request.getName().isBlank()) {
            admin.setName(request.getName());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            admin.setRole(request.getRole());
        }
        

		logsRepository.save(
		            new Logs(
		                    "UPDATE_ADMIN",
		                    adminEmail,
		                    "ADMIN",
		                    adminId,
		                    LocalDateTime.now()
		            )
		);
		
        return adminRepository.save(admin);
    }

    /**
     * An admin can delete their own account.
     * No dependent rows reference the admin table, so a plain delete is safe.
     */
    public void deleteAdmin(Long adminId, String adminEmail) {

    	if (!adminRepository.existsById(adminId)) {
            throw new ResourceNotFoundException("Admin not found: " + adminId);
        }

        adminRepository.deleteById(adminId);
        logsRepository.save(
	            new Logs(
	                    "DELETE_ADMIN",
	                    adminEmail,
	                    "ADMIN",
	                    adminId,
	                    LocalDateTime.now()
	            )
	);
    }
    
    public static String buildAvailabilityJson(String startTime, String endTime) {
        return "{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"}";
    }
}