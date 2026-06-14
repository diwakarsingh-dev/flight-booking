package com.flight.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int availableSeats;
}
