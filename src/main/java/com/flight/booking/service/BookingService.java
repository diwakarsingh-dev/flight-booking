package com.flight.booking.service;

import com.flight.booking.dto.BookingRequest;
import com.flight.booking.dto.BookingResponse;
import com.flight.booking.exception.FlightNotFoundException;
import com.flight.booking.exception.InsufficientSeatsException;
import com.flight.booking.model.Booking;
import com.flight.booking.model.Flight;
import com.flight.booking.repository.BookingRepository;
import com.flight.booking.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    public BookingResponse bookFlight(BookingRequest request) {
        log.info("Booking request received - flightNumber: {}, passengers: {}",
                request.flightNumber(), request.passengerNames());

        if (flightRepository.findByFlightNumber(request.flightNumber()).isEmpty()) {
            log.warn("Flight not found: {}", request.flightNumber());
            throw new FlightNotFoundException("Flight not found: " + request.flightNumber());
        }

        AtomicReference<Integer> seatsRemaining = new AtomicReference<>();

        Flight updatedFlight = flightRepository.getStore().compute(request.flightNumber(), (key, flight) -> {
            if (flight == null) {
                throw new FlightNotFoundException("Flight not found: " + key);
            }
            if (request.numberOfSeats() > flight.getAvailableSeats()) {
                throw new InsufficientSeatsException(
                        String.format("Requested %d seats but only %d available on flight %s",
                                request.numberOfSeats(), flight.getAvailableSeats(), key));
            }
            flight.setAvailableSeats(flight.getAvailableSeats() - request.numberOfSeats());
            seatsRemaining.set(flight.getAvailableSeats());
            return flight;
        });

        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID())
                .flightNumber(request.flightNumber())
                .passengerNames(request.passengerNames())
                .passengerEmail(request.passengerEmail())
                .numberOfSeats(request.numberOfSeats())
                .bookingTime(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        log.info("Booking successful - bookingId: {}, seatsRemaining: {}",
                booking.getBookingId(), seatsRemaining.get());

        return new BookingResponse(
                booking.getBookingId(),
                booking.getFlightNumber(),
                booking.getPassengerNames(),
                booking.getNumberOfSeats(),
                booking.getBookingTime(),
                "Booking confirmed"
        );
    }
}
