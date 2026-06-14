package com.flight.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingRequest(

        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotBlank(message = "Passenger name is required")
        String passengerName,

        @NotBlank(message = "Passenger email is required")
        @Email(message = "Invalid email format")
        String passengerEmail,

        @Min(value = 1, message = "Number of seats must be at least 1")
        int numberOfSeats
) {
}
