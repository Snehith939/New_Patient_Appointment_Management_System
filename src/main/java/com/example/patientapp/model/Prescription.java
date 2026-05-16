package com.example.patientapp.model;
 
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import jakarta.persistence.*;
 
import java.time.LocalDate;
import java.util.List;
 
@Entity
@Table(name = "prescription")
@Data
public class Prescription {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionId;
 
    // ✅ Fix patient serialization
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private Patient patient;
 
    // ✅ Fix doctor serialization
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"availability"})
    private Doctor doctor;
 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIgnoreProperties({"patient", "doctor"})
    private Appointment appointment;
 
    private Integer age;
 
    private LocalDate prescribedDate;
 
    // ✅ Fix infinite loop
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL,orphanRemoval=true)
    @JsonManagedReference
    private List<PrescriptionItem> items;
}


















































//package com.example.patientapp.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "prescription")
//@Data
//public class Prescription {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long prescriptionId;
//
//    // FK -> patient.patientId
//    @ManyToOne
//    @JoinColumn(name = "patientId", nullable = false)
//    private Patient patient;
//
//    // FK -> doctor.doctorId
//    @ManyToOne
//    @JoinColumn(name = "doctorId", nullable = false)
//    private Doctor doctor;
//
//    private String medicineName;
//    private String dose;
//    private String numberOfDays;
//    private String instructions;  // optional extra notes (e.g. "take after meals")
//
//    private LocalDate prescribedDate;
//}