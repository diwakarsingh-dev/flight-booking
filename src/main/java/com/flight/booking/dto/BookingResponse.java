package com.flight.booking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String flightNumber,
        List<String> passengerNames,
        int numberOfSeats,
        LocalDateTime bookingTime,
        String status
) {
}
