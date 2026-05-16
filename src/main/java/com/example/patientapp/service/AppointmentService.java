package com.example.patientapp.service;

import com.example.patientapp.dto.AppointmentResponse;
import com.example.patientapp.dto.BookAppointmentRequest;
import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import com.example.patientapp.model.BlockedSlot;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.BlockedSlotRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import com.example.patientapp.exception.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BlockedSlotRepository blockedSlotRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              BlockedSlotRepository blockedSlotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.blockedSlotRepository = blockedSlotRepository;
    }

    // — Available slots —

    /**
     * Returns all open 15-minute time slots for a doctor on a given date.
     *
     * Steps:
     *  1. Parse the doctor's availability JSON -> {"startTime":"09:00","endTime":"17:00"}
     *  2. Generate every 15-min slot in that window -> [09:00, 09:15, ..., 16:45]
     *  3. Remove slots already BOOKED/COMPLETED on that date (cancelled slots are freed)
     */
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        if (doctor.getAvailability() == null || doctor.getAvailability().isBlank()) {
            throw new BadRequestException("Doctor has not configured availability yet");
        }
        
        LocalTime[] window = parseAvailabilityJson(doctor.getAvailability());
        List<LocalTime> all = generateAllSlots(window[0], window[1]);

        // Slots taken by real appointments (BOOKED or COMPLETED - not CANCELED)
        Set<LocalTime> taken = appointmentRepository
                .findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
                        doctorId, date, AppointmentStatus.CANCELED)
                .stream()
                .map(Appointment::getTimeSlot)
                .collect(Collectors.toSet());

        // Slots the doctor has explicitly blocked (no patient involved)
        Set<LocalTime> blocked = blockedSlotRepository
                .findByDoctor_DoctorIdAndBlockedDate(doctorId, date)
                .stream()
                .map(BlockedSlot::getTimeSlot)
                .collect(Collectors.toSet());

        // A slot is available only if it is neither taken nor blocked
        return all.stream()
                .filter(slot -> !taken.contains(slot) && !blocked.contains(slot))
                .collect(Collectors.toList());
    }

    /**
     * Parses the availability JSON column value.
     * Input:  {"startTime":"09:00","endTime":"17:00"}
     * Output: [LocalTime(09:00), LocalTime(17:00)]
     */
    private LocalTime[] parseAvailabilityJson(String availability) {
        try {
            JsonNode node = objectMapper.readTree(availability);
            LocalTime start = LocalTime.parse(node.get("startTime").asText());
            LocalTime end   = LocalTime.parse(node.get("endTime").asText());
            return new LocalTime[]{start, end};
        } catch (Exception e) {
            throw new RuntimeException(
                    "Invalid availability format. Expected: {\"startTime\":\"HH:mm\",\"endTime\":\"HH:mm\"}");
        }
    }

    /**
     * Generates slots in 15-minute steps from start (inclusive) to end (exclusive).
     * e.g. 09:00 -> 17:00 produces [09:00, 09:15, 09:30, ..., 16:45]
     */
    private List<LocalTime> generateAllSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(15);
        }
        return slots;
    }

    // — Book appointment —
    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + req.getPatientId()));
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + req.getDoctorId()));

        List<LocalTime> available = getAvailableSlots(req.getDoctorId(), req.getAppointmentDate());
        if (!available.contains(req.getTimeSlot())) {
            throw new SlotConflictException(
                    "Slot " + req.getTimeSlot() + " on " + req.getAppointmentDate()
                            + " is not available for Dr. " + doctor.getName());
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setTimeSlot(req.getTimeSlot());
        appointment.setStatus(AppointmentStatus.BOOKED);

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    // — Cancel / view —
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));
        
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BadRequestException(
                    "Cannot cancel appointment with status: " + appointment.getStatus());
        }
        
        appointment.setStatus(AppointmentStatus.CANCELED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> viewAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(Long id) {
        return AppointmentResponse.from(
                appointmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id))
        );
    }
}