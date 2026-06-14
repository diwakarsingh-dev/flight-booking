package com.flight.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private UUID bookingId;
    private String flightNumber;
    private List<String> passengerNames;
    private String passengerEmail;
    private int numberOfSeats;
    private LocalDateTime bookingTime;
}
