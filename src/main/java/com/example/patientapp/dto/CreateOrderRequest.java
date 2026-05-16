package com.example.patientapp.dto;
import lombok.Data;
@Data
public class CreateOrderRequest {
	private Long appointmentId;
    private Integer amount;
    private String currency;
}
