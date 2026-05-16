package com.example.patientapp.dto;
import lombok.Data;
@Data
public class PaymentCallbackRequest {
	private String razorpay_order_id;
    private String razorpay_payment_id;
    private String razorpay_signature;
}
