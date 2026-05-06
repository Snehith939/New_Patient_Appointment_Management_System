package com.example.patientapp.repository;

import com.example.patientapp.model.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {

    // All blocked slots for a doctor on a given date
    // Used by getAvailableSlots() to filter out blocked times
    List<BlockedSlot> findByDoctor_DoctorIdAndBlockedDate(Long doctorId, LocalDate date);

    // Used to check existence before blocking, and to find the row before deleting
    Optional<BlockedSlot> findByDoctor_DoctorIdAndBlockedDateAndTimeSlot(
            Long doctorId, LocalDate date, LocalTime timeSlot);

    // Delete all blocked slots for a doctor before deleting the doctor row
    @Transactional
    void deleteByDoctor_DoctorId(Long doctorId);
}