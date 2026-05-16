package com.example.patientapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
 
@Entity
@Table(name = "prescription_item")
@Data
public class PrescriptionItem {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String medicineName;
    private Long numberOfDays;
    private String instructions;
 
    // ✅ CHANGED boolean → Boolean
    private Boolean morning;
    private Boolean afternoon;
    private Boolean night;
 
    @ManyToOne
    @JoinColumn(name = "prescription_id")
    @JsonBackReference
    private Prescription prescription;
    
}
