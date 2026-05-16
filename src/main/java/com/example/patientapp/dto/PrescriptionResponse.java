//package com.example.patientapp.dto;
//
//import com.example.patientapp.model.Prescription;
//import lombok.Data;
//
//import java.time.LocalDate;
//
///**
// * Flat response DTO for a prescription.
// * Avoids returning the full nested Patient and Doctor entities.
// */
//@Data
//public class PrescriptionResponse {
//
//    private Long prescriptionId;
//
//    // Patient summary
//    private Long   patientId;
//    private String patientName;
//
//    // Doctor summary
//    private Long   doctorId;
//    private String doctorName;
//    private String doctorSpecialization;
//
//    // Prescription details
//    private String    medicineName;
//    private String    dose;
//    private String    numberOfDays;
//    private String    instructions;
//    private LocalDate prescribedDate;
//
//    public static PrescriptionResponse from(Prescription p) {
//        PrescriptionResponse res = new PrescriptionResponse();
//        res.setPrescriptionId(p.getPrescriptionId());
//
//        res.setPatientId(p.getPatient().getPatientId());
//        res.setPatientName(p.getPatient().getName());
//
//        res.setDoctorId(p.getDoctor().getDoctorId());
//        res.setDoctorName(p.getDoctor().getName());
//        res.setDoctorSpecialization(p.getDoctor().getSpecialization());
//
//        res.setMedicineName(p.getMedicineName());
//        res.setDose(p.getDose());
//        res.setNumberOfDays(p.getNumberOfDays());
//        res.setInstructions(p.getInstructions());
//        res.setPrescribedDate(p.getPrescribedDate());
//
//        return res;
//    }
//}