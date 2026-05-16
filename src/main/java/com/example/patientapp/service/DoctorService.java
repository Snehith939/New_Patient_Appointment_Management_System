package com.example.patientapp.service;

import com.example.patientapp.dto.DoctorScheduleResponse;
import com.example.patientapp.model.BlockedSlot;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.BlockedSlotRepository;
import com.example.patientapp.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.patientapp.service.AuthService.buildAvailabilityJson;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final BlockedSlotRepository blockedSlotRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         BlockedSlotRepository blockedSlotRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.blockedSlotRepository = blockedSlotRepository;
    }

    // — List & add —

    public List<Doctor> getAllDoctors() { return doctorRepository.findAll(); }

    public Doctor addDoctor(Doctor doctor) { return doctorRepository.save(doctor); }

    // — Availability —

    /**
     * Updates the doctor's working window (start -> end).
     * Stored in the availability JSON column as {"startTime":"09:00","endTime":"17:00"}.
     */
    public Doctor updateDoctorAvailability(Long doctorId, String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end   = LocalTime.parse(endTime);
        if (!start.isBefore(end)) {
            throw new RuntimeException("startTime must be before endTime");
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        doctor.setAvailability(buildAvailabilityJson(startTime, endTime));
        return doctorRepository.save(doctor);
    }

    // — Schedule —

    /**
     * Returns today's appointments for the doctor, ordered by timeSlot ascending.
     * Uses DoctorScheduleResponse DTO so only patient name/phone + slot + status
     * are returned - not the full patient entity.
     */
    public List<DoctorScheduleResponse> getTodaySchedule(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found: " + doctorId);
        }
        return appointmentRepository
                .findByDoctor_DoctorIdAndAppointmentDateOrderByTimeSlotAsc(doctorId, LocalDate.now())
                .stream()
                .map(DoctorScheduleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns ALL appointments for the doctor (all dates), ordered by date and time.
     * Useful for a full schedule overview.
     */
    public List<DoctorScheduleResponse> getFullSchedule(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found: " + doctorId);
        }
        return appointmentRepository
                .findByDoctor_DoctorId(doctorId)
                .stream()
                .map(DoctorScheduleResponse::from)
                .collect(Collectors.toList());
    }

    // — Blocked slots —

    /**
     * Marks a specific time slot on a given date as blocked.
     * Once blocked, getAvailableSlots() in AppointmentService will not return
     * this slot to patients trying to book.
     *
     * Throws if the slot is already blocked (the DB unique constraint also
     * enforces this at the database level as a safety net).
     */
    public BlockedSlot blockSlot(Long doctorId, LocalDate date, LocalTime timeSlot) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        
        boolean appointmentExists =
                appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndTimeSlot(
                        doctorId, date, timeSlot);
     
        if (appointmentExists) {
            throw new RuntimeException(
                    "Patient has Appointment in this time slot. Choose different one.");
        }
        
        
        
        if (blockedSlotRepository
                .findByDoctor_DoctorIdAndBlockedDateAndTimeSlot(doctorId, date, timeSlot)
                .isPresent()) {
            throw new RuntimeException(
                    "Slot " + timeSlot + " on " + date + " is already blocked");
        }

        BlockedSlot blocked = new BlockedSlot();
        blocked.setDoctor(doctor);
        blocked.setBlockedDate(date);
        blocked.setTimeSlot(timeSlot);
        return blockedSlotRepository.save(blocked);
    }
    
    
    
    
    
    
    
    

    /**
     * Removes a previously blocked slot, making it bookable again.
     */
    public void unblockSlot(Long doctorId, LocalDate date, LocalTime timeSlot) {
        BlockedSlot slot = blockedSlotRepository
                .findByDoctor_DoctorIdAndBlockedDateAndTimeSlot(doctorId, date, timeSlot)
                .orElseThrow(() -> new RuntimeException(
                        "No blocked slot found for " + timeSlot + " on " + date));
        blockedSlotRepository.delete(slot);
    }
}