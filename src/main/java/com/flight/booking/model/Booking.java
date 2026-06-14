package com.flight.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private UUID bookingId;
    private String flightNumber;
    private String passengerName;
    private String passengerEmail;
    private int numberOfSeats;
    private LocalDateTime bookingTime;
}
