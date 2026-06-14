package com.flight.booking.repository;

import com.flight.booking.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class BookingRepository {

    private final ConcurrentHashMap<UUID, Booking> bookings = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        log.debug("Saving booking: {}", booking.getBookingId());
        bookings.put(booking.getBookingId(), booking);
        return booking;
    }

    public Optional<Booking> findByBookingId(UUID bookingId) {
        log.debug("Finding booking by id: {}", bookingId);
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public Collection<Booking> findAll() {
        log.debug("Retrieving all bookings");
        return bookings.values();
    }

    public void delete(UUID bookingId) {
        log.debug("Deleting booking: {}", bookingId);
        bookings.remove(bookingId);
    }
}
