package com.example.patientapp.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.patientapp.exception.BadRequestException;
import com.example.patientapp.exception.ResourceNotFoundException;
import com.example.patientapp.exception.UnauthorizedActionException;
import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.model.Patient;
import com.example.patientapp.model.Prescription;
import com.example.patientapp.model.PrescriptionItem;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.DoctorRepository;
import com.example.patientapp.repository.PatientRepository;
import com.example.patientapp.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

   @Autowired
   private PrescriptionRepository prescriptionRepository;

   @Autowired
   private PatientRepository patientRepository;

   @Autowired
   private DoctorRepository doctorRepository;

   @Autowired
   private AppointmentRepository appointmentRepository;

   // ✅ ADD PRESCRIPTION
   public Prescription addPrescription(Prescription prescription,
                                       Long doctorId,
                                       Long appointmentId) {

       Appointment appointment = appointmentRepository.findById(appointmentId)
               .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

       Doctor doctor = doctorRepository.findById(doctorId)
               .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

       if (appointment.getDoctor() == null) {
           throw new BadRequestException("No doctor assigned to this appointment");
       }

       if (!appointment.getDoctor().getDoctorId().equals(doctorId)) {
           throw new UnauthorizedActionException("You are not authorized to create this prescription");
       }

       if (prescriptionRepository.existsByAppointmentAppointmentId(appointmentId)) {
           throw new BadRequestException("Prescription already exists for this appointment");
       }

       Patient patient = appointment.getPatient();

       prescription.setAppointment(appointment);
       prescription.setDoctor(doctor);
       prescription.setPatient(patient);

       if (prescription.getPrescribedDate() == null) {
           prescription.setPrescribedDate(LocalDate.now());
       }

       if (prescription.getItems() != null) {
           prescription.getItems().forEach(item -> item.setPrescription(prescription));
       }

       return prescriptionRepository.save(prescription);
   }

   // ✅ GET ALL
   public List<Prescription> getAllPrescriptions(Long patientId) {
       return prescriptionRepository.findByPatientPatientId(patientId);
   }

   // ✅ GET ONE
   public Prescription getPrescription(Long prescriptionId) {
       return prescriptionRepository.findById(prescriptionId)
               .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
   }

   // ✅ DELETE
   public void deletePrescription(Long prescriptionId, Long doctorId) {

       Prescription prescription = prescriptionRepository.findById(prescriptionId)
               .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

       if (!prescription.getDoctor().getDoctorId().equals(doctorId)) {
           throw new UnauthorizedActionException(
                   "You are not authorized to delete this prescription");
       }

       prescriptionRepository.delete(prescription);
   }

   // ✅ UPDATE (FULL FEATURE: add + update + delete)
   public Prescription updatePrescription(Long prescriptionId,
                                          Long doctorId,
                                          Prescription updatedPrescription) {

       Prescription existing = prescriptionRepository.findById(prescriptionId)
               .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

       if (!existing.getDoctor().getDoctorId().equals(doctorId)) {
           throw new UnauthorizedActionException(
                   "You are not authorized to update this prescription");
       }

       //  Update date
       if (updatedPrescription.getPrescribedDate() != null) {
           existing.setPrescribedDate(updatedPrescription.getPrescribedDate());
       }

       if (updatedPrescription.getItems() != null) {

           //  STEP 1: REMOVE items not present in payload
           existing.getItems().removeIf(existingItem ->
               updatedPrescription.getItems().stream()
                   .noneMatch(updatedItem ->
                           updatedItem.getId() != null &&
                           updatedItem.getId().equals(existingItem.getId()))
           );

           //  STEP 2: UPDATE or ADD
           for (PrescriptionItem updatedItem : updatedPrescription.getItems()) {

               if (updatedItem.getId() != null) {

                   // ✅ UPDATE EXISTING
                   PrescriptionItem existingItem = existing.getItems().stream()
                           .filter(item -> item.getId().equals(updatedItem.getId()))
                           .findFirst()
                           .orElseThrow(() ->
                                   new ResourceNotFoundException(
                                           "Item not found with ID: " + updatedItem.getId()));

                   if (updatedItem.getMedicineName() != null &&
                           !updatedItem.getMedicineName().trim().isEmpty()) {

                       existingItem.setMedicineName(updatedItem.getMedicineName().trim());
                   }

                   if (updatedItem.getInstructions() != null &&
                           !updatedItem.getInstructions().trim().isEmpty()) {

                       existingItem.setInstructions(updatedItem.getInstructions().trim());
                   }

                   if (updatedItem.getNumberOfDays() != null) {
                       if (updatedItem.getNumberOfDays() <= 0) {
                           throw new BadRequestException("numberOfDays must be greater than 0");
                       }
                       existingItem.setNumberOfDays(updatedItem.getNumberOfDays());
                   }

                   if (updatedItem.getMorning() != null) {
                       existingItem.setMorning(updatedItem.getMorning());
                   }
                   if (updatedItem.getAfternoon() != null) {
                       existingItem.setAfternoon(updatedItem.getAfternoon());
                   }
                   if (updatedItem.getNight() != null) {
                       existingItem.setNight(updatedItem.getNight());
                   }

               } else {

                   //  ADD NEW ITEM
                   if (updatedItem.getMedicineName() == null ||
                       updatedItem.getMedicineName().trim().isEmpty()) {

                       throw new BadRequestException("Medicine name is required for new item");
                   }

                   if (updatedItem.getNumberOfDays() == null ||
                       updatedItem.getNumberOfDays() <= 0) {

                       throw new BadRequestException("Valid numberOfDays required");
                   }

                   updatedItem.setMedicineName(updatedItem.getMedicineName().trim());

                   updatedItem.setPrescription(existing);
                   existing.getItems().add(updatedItem);
               }
           }
       }

       return prescriptionRepository.save(existing);
   }
}
//
//import com.example.patientapp.dto.PrescriptionRequest;
//import com.example.patientapp.dto.PrescriptionResponse;
//import com.example.patientapp.model.Doctor;
//import com.example.patientapp.model.Patient;
//import com.example.patientapp.model.Prescription;
//import com.example.patientapp.repository.DoctorRepository;
//import com.example.patientapp.repository.PatientRepository;
//import com.example.patientapp.repository.PrescriptionRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class PrescriptionService {
//
//    private final PrescriptionRepository prescriptionRepository;
//    private final PatientRepository patientRepository;
//    private final DoctorRepository doctorRepository;
//
//    public PrescriptionService(PrescriptionRepository prescriptionRepository,
//                               PatientRepository patientRepository,
//                               DoctorRepository doctorRepository) {
//        this.prescriptionRepository = prescriptionRepository;
//        this.patientRepository = patientRepository;
//        this.doctorRepository = doctorRepository;
//    }
//
//    // — Create —
//
//    /**
//     * Doctor issues a new prescription to a patient.
//     * prescribedDate is set to today automatically.
//     */
//    public PrescriptionResponse createPrescription(PrescriptionRequest req) {
//        Patient patient = patientRepository.findById(req.getPatientId())
//                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
//        Doctor doctor = doctorRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        Prescription prescription = new Prescription();
//        prescription.setPatient(patient);
//        prescription.setDoctor(doctor);
//        prescription.setMedicineName(req.getMedicineName());
//        prescription.setDose(req.getDose());
//        prescription.setNumberOfDays(req.getNumberOfDays());
//        prescription.setInstructions(req.getInstructions());
//        prescription.setPrescribedDate(LocalDate.now());
//
//        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
//    }
//
//    // — Read —
//
//    /**
//     * Patient retrieves all their prescriptions (all doctors, all dates).
//     */
//    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
//        if (!patientRepository.existsById(patientId)) {
//            throw new RuntimeException("Patient not found: " + patientId);
//        }
//        return prescriptionRepository.findByPatient_PatientId(patientId)
//                .stream()
//                .map(PrescriptionResponse::from)
//                .collect(Collectors.toList());
//    }
//
//    // — Update —
//
//    /**
//     * Doctor updates an existing prescription.
//     * Only non-null, non-blank fields in the request overwrite the stored value.
//     * patientId and doctorId are NOT re-assignable - a prescription's ownership
//     * is fixed at creation time.
//     */
//    public PrescriptionResponse updatePrescription(Long prescriptionId, PrescriptionRequest req) {
//        Prescription prescription = prescriptionRepository.findById(prescriptionId)
//                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));
//
//        if (req.getMedicineName() != null && !req.getMedicineName().isBlank()) {
//            prescription.setMedicineName(req.getMedicineName());
//        }
//        if (req.getDose() != null && !req.getDose().isBlank()) {
//            prescription.setDose(req.getDose());
//        }
//        if (req.getNumberOfDays() != null && !req.getNumberOfDays().isBlank()) {
//            prescription.setNumberOfDays(req.getNumberOfDays());
//        }
//        if (req.getInstructions() != null && !req.getInstructions().isBlank()) {
//            prescription.setInstructions(req.getInstructions());
//        }
//
//        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
//    }
//
//    // — Delete —
//
//    public void deletePrescription(Long prescriptionId) {
//        if (!prescriptionRepository.existsById(prescriptionId)) {
//            throw new RuntimeException("Prescription not found: " + prescriptionId);
//        }
//        prescriptionRepository.deleteById(prescriptionId);
//    }
//}