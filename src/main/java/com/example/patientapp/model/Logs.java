package com.example.patientapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
@Data
public class Logs {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;        // DELETE_PATIENT, DELETE_DOCTOR, etc.

    private String performedBy;   // Admin name or email

    private String targetType;    // PATIENT / DOCTOR / ADMIN

    private Long targetId;

    private LocalDateTime timestamp;
    
    public Logs() {}


	public Logs(String action, String performedBy,
	                    String targetType, Long targetId,
	                    LocalDateTime timestamp) {
	        this.action = action;
	        this.performedBy = performedBy;
	        this.targetType = targetType;
	        this.targetId = targetId;
	        this.timestamp = timestamp;
	    }

    

}
