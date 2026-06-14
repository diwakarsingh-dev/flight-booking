package com.flight.booking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String flightNumber,
        String passengerName,
        int numberOfSeats,
        LocalDateTime bookingTime,
        String status
) {
}
