package com.example.patientapp.service;

import com.example.patientapp.dto.LoginRequest;
import com.example.patientapp.dto.RegisterRequest;
import com.example.patientapp.model.Admin;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.model.Role;
import com.example.patientapp.repository.AdminRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;

    public AuthService(PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       AdminRepository adminRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
    }

    // — Register —

    public Object register(RegisterRequest req) {
        if (req.getRole() == null) {
            throw new RuntimeException("role is required: PATIENT, DOCTOR, or ADMIN");
        }
        return switch (req.getRole()) {
            case PATIENT -> registerPatient(req);
            case DOCTOR  -> registerDoctor(req);
            case ADMIN   -> registerAdmin(req);
        };
    }

    private Patient registerPatient(RegisterRequest req) {
        Patient p = new Patient();
        p.setName(req.getName());
        p.setEmail(req.getEmail());
        p.setPassword(req.getPassword());
        p.setPhone(req.getPhone());
        p.setAddress(req.getAddress());
        p.setDob(req.getDob());
        p.setRole(Role.PATIENT);
        return patientRepository.save(p);
    }

    private Doctor registerDoctor(RegisterRequest req) {
        Doctor d = new Doctor();
        d.setName(req.getName());
        d.setEmail(req.getEmail());
        d.setPassword(req.getPassword());
        d.setPhone(req.getPhone());
        d.setSpecialization(req.getSpecialization());

        // Build the JSON string that goes into the availability column
        if (req.getStartTime() != null && req.getEndTime() != null) {
            d.setAvailability(buildAvailabilityJson(req.getStartTime(), req.getEndTime()));
        }
        return doctorRepository.save(d);
    }

    private Admin registerAdmin(RegisterRequest req) {
        Admin a = new Admin();
        a.setName(req.getName());
        a.setEmail(req.getEmail());
        a.setPassword(req.getPassword());
        a.setRole(req.getAdminRole());
        return adminRepository.save(a);
    }

    // — Login —

    public String login(LoginRequest req) {
        if (req.getRole() == null) {
            throw new RuntimeException("role is required: PATIENT, DOCTOR, or ADMIN");
        }
        return switch (req.getRole()) {
            case PATIENT -> {
                Patient p = patientRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException("No patient found with email: " + req.getEmail()));
                if (!p.getPassword().equals(req.getPassword())) throw new RuntimeException("Incorrect password");
                yield "Login successful. Welcome, " + p.getName() + " [PATIENT]";
            }
            case DOCTOR -> {
                Doctor d = doctorRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException("No doctor found with email: " + req.getEmail()));
                if (!d.getPassword().equals(req.getPassword())) throw new RuntimeException("Incorrect password");
                yield "Login successful. Welcome, Dr. " + d.getName() + " [DOCTOR]";
            }
            case ADMIN -> {
                Admin a = adminRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException("No admin found with email: " + req.getEmail()));
                if (!a.getPassword().equals(req.getPassword())) throw new RuntimeException("Incorrect password");
                yield "Login successful. Welcome, " + a.getName() + " [ADMIN]";
            }
        };
    }

    // — Helper —

    /**
     * Produces the JSON string stored in the availability column.
     * e.g. buildAvailabilityJson("09:00", "17:00")
     *   -> {"startTime":"09:00","endTime":"17:00"}
     */
    public static String buildAvailabilityJson(String startTime, String endTime) {
        return "{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"}";
    }
}