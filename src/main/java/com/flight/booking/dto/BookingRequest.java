package com.flight.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BookingRequest(

        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotEmpty(message = "At least one passenger name is required")
        List<@NotBlank(message = "Passenger name must not be blank") String> passengerNames,

        @NotBlank(message = "Passenger email is required")
        @Email(message = "Invalid email format")
        String passengerEmail
) {
        public int numberOfSeats() {
                return passengerNames == null ? 0 : passengerNames.size();
        }
}
