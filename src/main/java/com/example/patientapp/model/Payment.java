package com.example.patientapp.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table(name = "payment")
@Data
public class Payment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    // FK -> appointment.appointmentId
    @ManyToOne
    @JoinColumn(name = "appointmentId", nullable = false)
    private Appointment appointment;

    @Column(unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    // Amount in paise (e.g. ₹500 = 50000 paise)
    private Integer amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
