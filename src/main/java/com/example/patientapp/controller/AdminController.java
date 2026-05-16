package com.example.patientapp.controller;

import com.example.patientapp.dto.RegisterRequest;
import com.example.patientapp.dto.UpdateAdminRequest;
import com.example.patientapp.model.Admin;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Logs;
import com.example.patientapp.model.Patient;
import com.example.patientapp.service.AdminService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin management endpoints.
 * Register / login -> AuthController (/api/auth/...)
 *
 * Full endpoint map:
 * GET    /api/admins                  List all admins
 * GET    /api/admins/patients         List all patients
 * GET    /api/admins/doctors          List all doctors
 * DELETE /api/admins/patients/{id}    Delete a patient (cascades appointments)
 * DELETE /api/admins/doctors/{id}     Delete a doctor (cascades appointments + blocked slots)
 * PUT    /api/admins/{id}             Update own name / job title
 * DELETE /api/admins/{id}             Delete self
 */

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    
    //view logs
    @GetMapping("/logs")
    public ResponseEntity<List<Logs>> getAllLogs() {
        return ResponseEntity.ok(adminService.getAllLogs());
    }
    
    
    
    
    // ---- View all admins ----

    /**
     * GET /api/admins
     * Returns every registered admin (excluding passwords - @JsonIgnore on model).
     */
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    // ---- Patient management ----

    /**
     * GET /api/admins/patients
     * Full list of all registered patients.
     */
    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    /**
     * DELETE /api/admins/patients/{id}
     * Deletes the patient and all of their appointment records.
     * Returns 200 with a confirmation message on success.
     */
    @DeleteMapping("/patients/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id, @RequestParam String adminEmail) {
        adminService.deletePatient(id,adminEmail);
        return ResponseEntity.ok("Patient " + id + " and all their appointments have been deleted.");
    }

    // ---- Doctor management ----

    /**
     * GET /api/admins/doctors
     * Full list of all registered doctors.
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }
    
  //this part has to implement after intrime
    
//    @PostMapping("/doctors")
//    public ResponseEntity<Object> createDoctor(@RequestBody RegisterRequest request) {
//        Object saved = adminService.createDoctor(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//    }

    /**
     * DELETE /api/admins/doctors/{id}
     * Deletes the doctor along with all their appointments and blocked slots.
     * Returns 200 with a confirmation message on success.
     */
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id,@RequestParam String adminEmail) {
        adminService.deleteDoctor(id,adminEmail);
        return ResponseEntity.ok(
                "Doctor " + id + ", their appointments, and blocked slots have been deleted."
        );
    }

    // ---- Admin self-management ----

    /**
     * PUT /api/admins/{id}
     * Body: { "name": "New Name", "role": "MANAGER" }
     *
     * An admin updates their own display name and/or job title.
     * Both fields are optional - omit a field to leave it unchanged.
     */
    
  //this part has to implement after intrime
    
//    @PostMapping
//    public ResponseEntity<Object> createAdmin(@RequestBody RegisterRequest request) {
//        Object saved = adminService.createAdmin(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//    } 
    
    
    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(
            @PathVariable Long id,
            @RequestBody UpdateAdminRequest request,
            @RequestParam String adminEmail) {
        return ResponseEntity.ok(adminService.updateAdmin(id, request,adminEmail));
    }

    /**
     * DELETE /api/admins/{id}
     * An admin deletes their own account.
     * Returns 200 with a confirmation message on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id, @RequestParam String adminEmail) {
        adminService.deleteAdmin(id,adminEmail);
        return ResponseEntity.ok("Admin account " + id + " has been deleted.");
    }
}