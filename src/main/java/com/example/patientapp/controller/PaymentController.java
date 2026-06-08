package com.example.patientapp.controller;
import com.example.patientapp.dto.CreateOrderRequest;
import com.example.patientapp.dto.PaymentCallbackRequest;
import com.example.patientapp.dto.PaymentResponse;
import com.example.patientapp.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/payments")
public class PaymentController {
	private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // — Create Order ——————————————————————————————————————————

    /**
     * POST /payments/order/create
     * Body: { "appointmentId": 1, "amount": 50000, "currency": "INR" }
     *
     * Creates a Razorpay order and persists it in the database.
     * Amount is in paise (₹500 = 50000). Currency defaults to "INR".
     */
    @PostMapping("/order/create")
    public ResponseEntity<PaymentResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createOrder(request));
    }

    // — Get Order Details —————————————————————————————————————

    /**
     * GET /payments/order/{orderId}
     *
     * Fetches payment details by Razorpay order ID (e.g. order_xxxxxxxxxxxxxxx).
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getOrderByOrderId(orderId));
    }

    // — Checkout Page —————————————————————————————————————————

    /**
     * GET /payments/checkout/{appointmentId}
     *
     * Returns structured payment details for the given appointment.
     * Must create an order first via POST /payments/order/create.
     */
    @GetMapping(value = "/checkout/{appointmentId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkout(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getCheckoutPage(appointmentId));
    }

    // — Store & Verify Payment ————————————————————————————————

    /**
     * POST /payments/store
     * Body: {
     *   "razorpay_order_id":   "order_xxx",
     *   "razorpay_payment_id": "pay_xxx",
     *   "razorpay_signature":  "xxx"
     * }
     *
     * Verifies the Razorpay signature and stores the payment result.
     */
    @PostMapping("/store")
    public ResponseEntity<PaymentResponse> storePayment(@RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(paymentService.storePayment(request));
    }

    // — Payment History ———————————————————————————————————————

    /**
     * GET /payments/history/{patientId}
     *
     * Retrieves all payment records for a patient, ordered newest first.
     */
    @GetMapping("/history/{patientId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(patientId));
    }

    // — Razorpay Key Config ——————————————————————————————————
    @GetMapping("/config")
    public ResponseEntity<java.util.Map<String, String>> getConfig() {
        return ResponseEntity.ok(java.util.Map.of("key", paymentService.getRazorpayKeyId()));
    }

    // — Record Failed Payment —————————————————————————————————
    @PostMapping("/failed")
    public ResponseEntity<PaymentResponse> recordFailedPayment(@RequestBody java.util.Map<String, String> payload) {
        String orderId = payload.get("razorpay_order_id");
        return ResponseEntity.ok(paymentService.recordFailedPayment(orderId));
    }

    // — Process Refund ————————————————————————————————————————
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestBody com.example.patientapp.dto.RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(request));
    }
}
