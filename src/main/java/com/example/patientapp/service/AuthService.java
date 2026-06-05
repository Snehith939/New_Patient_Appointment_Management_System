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

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    // — Register (NO CHANGES) —

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
        if (req.getStartTime() != null && req.getEndTime() != null) {
            d.setAvailability(buildAvailabilityJson(
                    req.getStartTime(), req.getEndTime()));
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

    // — Login (with HttpSession) —

    public Object login(LoginRequest req, HttpSession session) {
        if (req.getRole() == null) {
            throw new RuntimeException("role is required: PATIENT, DOCTOR, or ADMIN");
        }

        return switch (req.getRole()) {

            case PATIENT -> {
                Patient p = patientRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException(
                                "No patient found with email: " + req.getEmail()));

                if (!p.getPassword().equals(req.getPassword())) {
                    throw new RuntimeException("Incorrect password");
                }

                session.setAttribute("userId", p.getPatientId());
                session.setAttribute("role", "PATIENT");
                session.setAttribute("name", p.getName());
                session.setAttribute("email", p.getEmail());

                Map<String, Object> map = new HashMap<>();
                map.put("id",    p.getPatientId());
                map.put("name",  p.getName());
                map.put("email", p.getEmail());
                map.put("role",  "PATIENT");
                yield map;
            }

            case DOCTOR -> {
                Doctor d = doctorRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException(
                                "No doctor found with email: " + req.getEmail()));

                if (!d.getPassword().equals(req.getPassword())) {
                    throw new RuntimeException("Incorrect password");
                }

                session.setAttribute("userId", d.getDoctorId());
                session.setAttribute("role", "DOCTOR");
                session.setAttribute("name", d.getName());
                session.setAttribute("email", d.getEmail());

                Map<String, Object> map = new HashMap<>();
                map.put("id",    d.getDoctorId());
                map.put("name",  d.getName());
                map.put("email", d.getEmail());
                map.put("role",  "DOCTOR");
                yield map;
            }

            case ADMIN -> {
                Admin a = adminRepository.findByEmail(req.getEmail())
                        .orElseThrow(() -> new RuntimeException(
                                "No admin found with email: " + req.getEmail()));

                if (!a.getPassword().equals(req.getPassword())) {
                    throw new RuntimeException("Incorrect password");
                }

                session.setAttribute("userId", a.getAdminId());
                session.setAttribute("role", "ADMIN");
                session.setAttribute("name", a.getName());
                session.setAttribute("email", a.getEmail());

                Map<String, Object> map = new HashMap<>();
                map.put("id",    a.getAdminId());
                map.put("name",  a.getName());
                map.put("email", a.getEmail());
                map.put("role",  "ADMIN");
                yield map;
            }
        };
    }

    // — Logout —

    public void logout(HttpSession session) {
        session.invalidate();
    }

    // — Helper —

    public static String buildAvailabilityJson(String startTime, String endTime) {
        return "{\"startTime\":\"" + startTime
                + "\",\"endTime\":\"" + endTime + "\"}";
    }
}