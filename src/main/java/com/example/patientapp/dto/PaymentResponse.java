package com.example.patientapp.dto;
import com.example.patientapp.model.Payment;
import com.example.patientapp.model.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PaymentResponse {
	private Long paymentId;
    private Long appointmentId;
    private String patientName;
    private String doctorName;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private Integer amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Payment entity to this DTO.
     * Called in PaymentService — never in the controller.
     */
    public static PaymentResponse from(Payment payment) {
        PaymentResponse res = new PaymentResponse();
        res.setPaymentId(payment.getPaymentId());
        res.setAppointmentId(payment.getAppointment().getAppointmentId());
        res.setPatientName(payment.getAppointment().getPatient().getName());
        res.setDoctorName(payment.getAppointment().getDoctor().getName());
        res.setRazorpayOrderId(payment.getRazorpayOrderId());
        res.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        res.setAmount(payment.getAmount());
        res.setCurrency(payment.getCurrency());
        res.setStatus(payment.getStatus());
        res.setCreatedAt(payment.getCreatedAt());
        res.setUpdatedAt(payment.getUpdatedAt());
        return res;
    }
}
