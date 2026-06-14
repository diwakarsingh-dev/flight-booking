package com.flight.booking.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(String message) {
        super(message);
    }
}
