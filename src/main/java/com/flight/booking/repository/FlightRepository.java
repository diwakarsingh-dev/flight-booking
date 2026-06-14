package com.flight.booking.repository;

import com.flight.booking.model.Flight;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class FlightRepository {

    private final ConcurrentHashMap<String, Flight> flights = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.debug("Pre-loading sample flights");

        save(Flight.builder()
                .flightNumber("FL001")
                .origin("New York")
                .destination("London")
                .departureTime(LocalDateTime.now().plusDays(1))
                .totalSeats(20)
                .availableSeats(20)
                .build());

        save(Flight.builder()
                .flightNumber("FL002")
                .origin("Los Angeles")
                .destination("Tokyo")
                .departureTime(LocalDateTime.now().plusDays(2))
                .totalSeats(300)
                .availableSeats(300)
                .build());

        save(Flight.builder()
                .flightNumber("FL003")
                .origin("Chicago")
                .destination("Paris")
                .departureTime(LocalDateTime.now().plusDays(3))
                .totalSeats(250)
                .availableSeats(250)
                .build());

        save(Flight.builder()
                .flightNumber("FL004")
                .origin("San Francisco")
                .destination("Sydney")
                .departureTime(LocalDateTime.now().plusDays(4))
                .totalSeats(350)
                .availableSeats(350)
                .build());

        save(Flight.builder()
                .flightNumber("FL005")
                .origin("Miami")
                .destination("Dubai")
                .departureTime(LocalDateTime.now().plusDays(5))
                .totalSeats(280)
                .availableSeats(280)
                .build());

        log.debug("Loaded {} sample flights", flights.size());
    }

    public Flight save(Flight flight) {
        log.debug("Saving flight: {}", flight.getFlightNumber());
        flights.put(flight.getFlightNumber(), flight);
        return flight;
    }

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        log.debug("Finding flight by number: {}", flightNumber);
        return Optional.ofNullable(flights.get(flightNumber));
    }

    public Collection<Flight> findAll() {
        log.debug("Retrieving all flights");
        return flights.values();
    }

    public void delete(String flightNumber) {
        log.debug("Deleting flight: {}", flightNumber);
        flights.remove(flightNumber);
    }

    public ConcurrentHashMap<String, Flight> getStore() {
        return flights;
    }
}
