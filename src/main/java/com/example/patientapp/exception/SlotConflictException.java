package com.example.patientapp.exception;
 
/**
* Thrown when a patient tries to book a slot
* that is already booked or blocked.
*
* This will map to HTTP 409 (Conflict).
*/
public class SlotConflictException extends RuntimeException {
 
    public SlotConflictException(String message) {
        super(message);
    }
}