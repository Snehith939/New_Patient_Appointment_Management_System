package com.example.patientapp;

import com.example.patientapp.dto.*;
import com.example.patientapp.model.*;
import com.example.patientapp.repository.*;
import com.example.patientapp.service.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class PaymentGatedAppointmentIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private RazorpayClient razorpayClient;

    private Patient patient;
    private Doctor doctor;
    private LocalDate date;
    private LocalTime slot;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @BeforeEach
    public void setUp() {
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();

        // 1. Create Patient
        patient = new Patient();
        patient.setName("Jane Doe");
        patient.setEmail("jane@example.com");
        patient.setPassword("hashedpassword");
        patient.setPhone("1234567890");
        patient = patientRepository.save(patient);

        // 2. Create Doctor
        doctor = new Doctor();
        doctor.setName("Dr. Smith");
        doctor.setEmail("smith@example.com");
        doctor.setPassword("hashedpassword");
        doctor.setPhone("0987654321");
        doctor.setSpecialization("Cardiologist");
        doctor.setAvailability("{\"startTime\":\"09:00\",\"endTime\":\"12:00\"}");
        doctor = doctorRepository.save(doctor);

        date = LocalDate.now().plusDays(1);
        slot = LocalTime.of(9, 0);
    }

    private String calculateHMACSignature(String orderId, String paymentId, String secret) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal((orderId + "|" + paymentId).getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPaymentGatedAppointmentBookingFlow() throws Exception {
        // Mock Razorpay Client behavior for order creation
        Order mockOrder = Mockito.mock(Order.class);
        when(mockOrder.get("id")).thenReturn("order_test123");
        
        // Mocking the inner client fields using Mockito
        com.razorpay.OrderClient orderClientMock = Mockito.mock(com.razorpay.OrderClient.class);
        razorpayClient.orders = orderClientMock;
        when(orderClientMock.create(any(JSONObject.class))).thenReturn(mockOrder);

        // 1. Initial booking: should be in PENDING_PAYMENT status
        BookAppointmentRequest bookReq = new BookAppointmentRequest();
        bookReq.setPatientId(patient.getPatientId());
        bookReq.setDoctorId(doctor.getDoctorId());
        bookReq.setAppointmentDate(date);
        bookReq.setTimeSlot(slot);

        AppointmentResponse apptResp = appointmentService.bookAppointment(bookReq);
        Assertions.assertNotNull(apptResp.getAppointmentId());
        Assertions.assertEquals(AppointmentStatus.PENDING_PAYMENT, apptResp.getStatus());

        // 2. The slot should STILL be available for booking by other patients (not locked yet)
        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(doctor.getDoctorId(), date);
        Assertions.assertTrue(availableSlots.contains(slot), "PENDING_PAYMENT slot should remain available");

        // 3. Create payment order: should transition payment state to CREATED
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setAppointmentId(apptResp.getAppointmentId());
        orderReq.setAmount(50000); // 500 INR in paise
        orderReq.setCurrency("INR");

        PaymentResponse payResp = paymentService.createOrder(orderReq);
        Assertions.assertEquals("order_test123", payResp.getRazorpayOrderId());
        Assertions.assertEquals(PaymentStatus.CREATED, payResp.getStatus());

        // 4. Successful Payment signature callback: should transition payment to PAID and appointment to BOOKED
        String paymentId = "pay_test123";
        String signature = calculateHMACSignature("order_test123", paymentId, razorpayKeySecret);

        PaymentCallbackRequest callbackReq = new PaymentCallbackRequest();
        callbackReq.setRazorpay_order_id("order_test123");
        callbackReq.setRazorpay_payment_id(paymentId);
        callbackReq.setRazorpay_signature(signature);

        PaymentResponse verifiedPay = paymentService.storePayment(callbackReq);
        Assertions.assertEquals(PaymentStatus.PAID, verifiedPay.getStatus());

        // Verify the appointment is now locked (BOOKED)
        Appointment updatedAppt = appointmentRepository.findById(apptResp.getAppointmentId()).orElseThrow();
        Assertions.assertEquals(AppointmentStatus.BOOKED, updatedAppt.getStatus());

        // Verify the slot is now removed from availability
        availableSlots = appointmentService.getAvailableSlots(doctor.getDoctorId(), date);
        Assertions.assertFalse(availableSlots.contains(slot), "BOOKED slot should no longer be available");
    }

    @Test
    public void testFailedPaymentFlow() throws Exception {
        // 1. Book appointment (PENDING_PAYMENT)
        BookAppointmentRequest bookReq = new BookAppointmentRequest();
        bookReq.setPatientId(patient.getPatientId());
        bookReq.setDoctorId(doctor.getDoctorId());
        bookReq.setAppointmentDate(date);
        bookReq.setTimeSlot(slot);
        AppointmentResponse apptResp = appointmentService.bookAppointment(bookReq);

        // Mock Razorpay order creation
        Order mockOrder = Mockito.mock(Order.class);
        when(mockOrder.get("id")).thenReturn("order_test456");
        com.razorpay.OrderClient orderClientMock = Mockito.mock(com.razorpay.OrderClient.class);
        razorpayClient.orders = orderClientMock;
        when(orderClientMock.create(any(JSONObject.class))).thenReturn(mockOrder);

        // Create payment order
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setAppointmentId(apptResp.getAppointmentId());
        orderReq.setAmount(50000);
        orderReq.setCurrency("INR");
        paymentService.createOrder(orderReq);

        // 2. Trigger Payment Failure endpoint
        paymentService.recordFailedPayment("order_test456");

        // Verify payment is FAILED and appointment is FAILED
        com.example.patientapp.model.Payment updatedPay = paymentRepository.findByRazorpayOrderId("order_test456").orElseThrow();
        Assertions.assertEquals(PaymentStatus.FAILED, updatedPay.getStatus());

        Appointment updatedAppt = appointmentRepository.findById(apptResp.getAppointmentId()).orElseThrow();
        Assertions.assertEquals(AppointmentStatus.FAILED, updatedAppt.getStatus());

        // Verify the slot remains available
        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(doctor.getDoctorId(), date);
        Assertions.assertTrue(availableSlots.contains(slot), "FAILED slot should remain available");
    }

    @Test
    public void testRefundFlow() throws Exception {
        // 1. Book appointment and set to PAID & BOOKED directly for mock test setup
        BookAppointmentRequest bookReq = new BookAppointmentRequest();
        bookReq.setPatientId(patient.getPatientId());
        bookReq.setDoctorId(doctor.getDoctorId());
        bookReq.setAppointmentDate(date);
        bookReq.setTimeSlot(slot);
        AppointmentResponse apptResp = appointmentService.bookAppointment(bookReq);

        Appointment appt = appointmentRepository.findById(apptResp.getAppointmentId()).orElseThrow();
        appt.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(appt);

        com.example.patientapp.model.Payment payment = new com.example.patientapp.model.Payment();
        payment.setAppointment(appt);
        payment.setRazorpayOrderId("order_test789");
        payment.setRazorpayPaymentId("pay_test789");
        payment.setAmount(50000);
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.PAID);
        payment = paymentRepository.save(payment);

        // 2. Mock Razorpay payment refund call
        com.razorpay.PaymentClient paymentClientMock = Mockito.mock(com.razorpay.PaymentClient.class);
        razorpayClient.payments = paymentClientMock;
        
        Refund mockRefund = Mockito.mock(Refund.class);
        when(paymentClientMock.refund(any(String.class), any(JSONObject.class))).thenReturn(mockRefund);

        // 3. Initiate refund
        RefundRequest refundReq = new RefundRequest();
        refundReq.setPaymentId(payment.getPaymentId());

        PaymentResponse refundResp = paymentService.refundPayment(refundReq);
        Assertions.assertEquals(PaymentStatus.REFUNDED, refundResp.getStatus());

        // Verify appointment status transitions to CANCELED
        Appointment refundedAppt = appointmentRepository.findById(apptResp.getAppointmentId()).orElseThrow();
        Assertions.assertEquals(AppointmentStatus.CANCELED, refundedAppt.getStatus());

        // Verify the slot is now open again
        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(doctor.getDoctorId(), date);
        Assertions.assertTrue(availableSlots.contains(slot), "Refunded slot should be open and available");
    }
}
