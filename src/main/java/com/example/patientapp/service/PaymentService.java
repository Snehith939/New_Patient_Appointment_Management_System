package com.example.patientapp.service;
 
import com.example.patientapp.dto.CreateOrderRequest;
import com.example.patientapp.dto.PaymentCallbackRequest;
import com.example.patientapp.dto.PaymentResponse;
import com.example.patientapp.dto.RefundRequest;
import com.example.patientapp.model.Appointment;
import com.example.patientapp.model.AppointmentStatus;
import com.example.patientapp.model.Payment;
import com.example.patientapp.model.PaymentStatus;
import com.example.patientapp.repository.AppointmentRepository;
import com.example.patientapp.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
 
@Service
public class PaymentService {
 
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final RazorpayClient razorpayClient;
 
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
 
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
 
    public PaymentService(PaymentRepository paymentRepository,
                          AppointmentRepository appointmentRepository,
                          AppointmentService appointmentService,
                          RazorpayClient razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.razorpayClient = razorpayClient;
    }
 
    // — Create Razorpay Order —
 
    /**
     * Creates a Razorpay order for the given appointment and persists it.
     *
     * Steps:
     *  1. Find the appointment by ID
     *  2. Build a Razorpay order request with amount, currency, receipt
     *  3. Call Razorpay API to create the order
     *  4. Save the Payment entity with status CREATED
     */
    public PaymentResponse createOrder(CreateOrderRequest req) {
        Appointment appointment = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + req.getAppointmentId()));
 
        // Check if a payment already exists for this appointment
        paymentRepository.findByAppointment_AppointmentId(req.getAppointmentId())
                .ifPresent(existing -> {
                    throw new RuntimeException("Payment already exists for appointment: " + req.getAppointmentId()
                            + " with order ID: " + existing.getRazorpayOrderId());
                });
 
        String currency = (req.getCurrency() != null && !req.getCurrency().isBlank())
                ? req.getCurrency() : "INR";
 
        try {
            // Build the Razorpay order request
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", req.getAmount());       // amount in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "appt_" + req.getAppointmentId());
 
            // Call Razorpay API
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
 
            // Persist in our database
            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setAmount(req.getAmount());
            payment.setCurrency(currency);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
 
            return PaymentResponse.from(paymentRepository.save(payment));
 
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }
 
    // — Get Order Details —
 
    /**
     * Fetches payment details by Razorpay order ID.
     */
    public PaymentResponse getOrderByOrderId(String orderId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return PaymentResponse.from(payment);
    }
 
    // — Checkout HTML Page —
 
    /**
     * Returns an HTML page embedding Razorpay checkout.js for the given appointment.
     * This page opens the Razorpay payment dialog when loaded in a browser.
     */
    public String getCheckoutPage(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException(
                        "No payment order found for appointment: " + appointmentId
                                + ". Create an order first via POST /payments/order/create"));
 
        Appointment appointment = payment.getAppointment();
 
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Payment Checkout</title>\n"
                + "    <script src=\"https://checkout.razorpay.com/v1/checkout.js\"></script>\n"
                + "</head>\n"
                + "<body>\n"
                + "    <h2>Processing Payment...</h2>\n"
                + "    <p>Appointment ID: " + appointmentId + "</p>\n"
                + "    <p>Patient: " + appointment.getPatient().getName() + "</p>\n"
                + "    <p>Doctor: " + appointment.getDoctor().getName() + "</p>\n"
                + "    <p>Amount: &#8377;" + (payment.getAmount() / 100.0) + "</p>\n"
                + "    <script>\n"
                + "        var options = {\n"
                + "            'key': '" + razorpayKeyId + "',\n"
                + "            'amount': '" + payment.getAmount() + "',\n"
                + "            'currency': '" + payment.getCurrency() + "',\n"
                + "            'name': 'Patient Appointment Management',\n"
                + "            'description': 'Appointment #" + appointmentId + "',\n"
                + "            'order_id': '" + payment.getRazorpayOrderId() + "',\n"
                + "            'handler': function (response) {\n"
                + "                fetch('/payments/store', {\n"
                + "                    method: 'POST',\n"
                + "                    headers: {'Content-Type': 'application/json'},\n"
                + "                    body: JSON.stringify({\n"
                + "                        razorpay_order_id: response.razorpay_order_id,\n"
                + "                        razorpay_payment_id: response.razorpay_payment_id,\n"
                + "                        razorpay_signature: response.razorpay_signature\n"
                + "                    })\n"
                + "                })\n"
                + "                .then(res => res.json())\n"
                + "                .then(data => {\n"
                + "                    document.body.innerHTML = '<h2>Payment Successful!</h2>'\n"
                + "                        + '<p>Payment ID: ' + data.razorpayPaymentId + '</p>'\n"
                + "                        + '<p>Order ID: ' + data.razorpayOrderId + '</p>'\n"
                + "                        + '<p>Status: ' + data.status + '</p>';\n"
                + "                })\n"
                + "                .catch(err => {\n"
                + "                    document.body.innerHTML = '<h2>Payment verification failed</h2>'\n"
                + "                        + '<p>' + err + '</p>';\n"
                + "                });\n"
                + "            },\n"
                + "            'prefill': {\n"
                + "                'name': '" + appointment.getPatient().getName() + "',\n"
                + "                'email': '" + appointment.getPatient().getEmail() + "'\n"
                + "            },\n"
                + "            'theme': {\n"
                + "                'color': '#3399cc'\n"
                + "            }\n"
                + "        };\n"
                + "        var rzp = new Razorpay(options);\n"
                + "        rzp.open();\n"
                + "    </script>\n"
                + "</body>\n"
                + "</html>";
    }
 
    // — Store & Verify Payment —
 
    /**
     * Verifies the Razorpay payment signature and updates payment status.
     *
     * Steps:
     *  1. Find the payment by Razorpay order ID
     *  2. Verify the HMAC SHA256 signature using Razorpay SDK utility
     *  3. Update payment status to PAID (or FAILED if verification fails)
     */
    public PaymentResponse storePayment(PaymentCallbackRequest req) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getRazorpay_order_id())
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for order: " + req.getRazorpay_order_id()));
 
        Appointment appointment = payment.getAppointment();
        try {
            // Verify the signature using Razorpay SDK utility
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", req.getRazorpay_order_id());
            attributes.put("razorpay_payment_id", req.getRazorpay_payment_id());
            attributes.put("razorpay_signature", req.getRazorpay_signature());
 
            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
 
            if (isValid) {
                // Check if the slot is still available before confirming booking
                List<java.time.LocalTime> availableSlots = appointmentService.getAvailableSlots(
                        appointment.getDoctor().getDoctorId(), appointment.getAppointmentDate());
                if (!availableSlots.contains(appointment.getTimeSlot())) {
                    // Auto-refund payment since slot was taken in the meantime
                    try {
                        JSONObject refundRequest = new JSONObject();
                        razorpayClient.payments.refund(req.getRazorpay_payment_id(), refundRequest);
                    } catch (Exception refundEx) {
                        System.err.println("Auto-refund failed: " + refundEx.getMessage());
                    }
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setRazorpayPaymentId(req.getRazorpay_payment_id());
                    payment.setRazorpaySignature(req.getRazorpay_signature());
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    appointment.setStatus(AppointmentStatus.FAILED);
                    appointmentRepository.save(appointment);

                    throw new RuntimeException("This time slot has already been booked and paid for by another patient. A refund has been automatically initiated.");
                }

                payment.setRazorpayPaymentId(req.getRazorpay_payment_id());
                payment.setRazorpaySignature(req.getRazorpay_signature());
                payment.setStatus(PaymentStatus.PAID);
                
                appointment.setStatus(AppointmentStatus.BOOKED);
                appointmentRepository.save(appointment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                appointment.setStatus(AppointmentStatus.FAILED);
                appointmentRepository.save(appointment);
            }
 
        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.FAILED);
            appointment.setStatus(AppointmentStatus.FAILED);
            appointmentRepository.save(appointment);
        }
 
        payment.setUpdatedAt(LocalDateTime.now());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // — Record Failed Payment —
    public PaymentResponse recordFailedPayment(String orderId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        if (payment.getStatus() == PaymentStatus.CREATED) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Appointment appointment = payment.getAppointment();
            appointment.setStatus(AppointmentStatus.FAILED);
            appointmentRepository.save(appointment);
        }
        return PaymentResponse.from(payment);
    }

    // — Process Refund —
    /**
     * Manually initiates a full refund for a PAID payment.
     *
     * VALIDATION: Refunds are only allowed if the appointment date has NOT yet
     * passed. Appointments whose date is already in the past are non-refundable.
     */
    public PaymentResponse refundPayment(RefundRequest req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + req.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only successful (PAID) payments can be refunded.");
        }

        // ── Date validation: block refunds for appointments that have already occurred ──
        Appointment appointment = payment.getAppointment();
        if (appointment.getAppointmentDate() != null
                && appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException(
                "Refund not allowed: the appointment on " + appointment.getAppointmentDate()
                + " has already passed. Only upcoming appointments can be refunded.");
        }

        try {
            JSONObject refundRequest = new JSONObject();
            // Call Razorpay API to issue the refund
            razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);

            return PaymentResponse.from(payment);

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    // — Auto-refund on Cancellation —
    /**
     * Called automatically when a BOOKED appointment is cancelled by the patient.
     *
     * Logic:
     *  - Looks up the payment for the given appointment.
     *  - If no PAID payment exists, returns false (nothing to refund).
     *  - If the appointment date has already PASSED, returns false — non-refundable.
     *  - Otherwise calls Razorpay to issue a full refund and marks the payment REFUNDED.
     *
     * This method never throws — Razorpay errors are logged and swallowed so that
     * the cancellation itself always succeeds even if the refund call fails.
     *
     * @return true if a refund was successfully issued, false otherwise.
     */
    public boolean initiateCancelRefundIfPaid(Long appointmentId) {
        Optional<Payment> paymentOpt =
                paymentRepository.findByAppointment_AppointmentId(appointmentId);
        if (paymentOpt.isEmpty()) {
            return false; // no payment record for this appointment
        }

        Payment payment = paymentOpt.get();

        // Only refund PAID payments
        if (payment.getStatus() != PaymentStatus.PAID) {
            return false;
        }

        // Must have a Razorpay payment ID to issue a refund
        if (payment.getRazorpayPaymentId() == null || payment.getRazorpayPaymentId().isBlank()) {
            return false;
        }

        // ── Date validation: do NOT refund if appointment date has already passed ──
        Appointment apt = payment.getAppointment();
        if (apt.getAppointmentDate() != null
                && apt.getAppointmentDate().isBefore(LocalDate.now())) {
            // Appointment already occurred — cancellation proceeds but no refund
            return false;
        }

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount());   // full refund in paise
            refundRequest.put("speed", "normal");
            refundRequest.put("notes",
                    new JSONObject().put("reason", "Appointment cancelled by patient"));
            refundRequest.put("receipt", "cancel_refund_" + payment.getPaymentId());

            razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            // Mark payment as REFUNDED in our database
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return true;

        } catch (RazorpayException e) {
            // Log the failure but never block the cancellation
            System.err.println("[PaymentService] Auto-refund failed for appointment "
                    + appointmentId + ": " + e.getMessage());
            return false;
        }
    }
 
    // — Payment History —
 
    /**
     * Returns all payments for a patient, ordered newest first.
     * Traverses: Payment -> Appointment -> Patient via the JPA relationship.
     */
    public List<PaymentResponse> getPaymentHistory(Long patientId) {
        return paymentRepository.findByAppointment_Patient_PatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // — Razorpay Key Configuration —
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}