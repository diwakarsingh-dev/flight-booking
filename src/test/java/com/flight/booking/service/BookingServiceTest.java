package com.flight.booking.service;

import com.flight.booking.dto.BookingRequest;
import com.flight.booking.dto.BookingResponse;
import com.flight.booking.exception.FlightNotFoundException;
import com.flight.booking.exception.InsufficientSeatsException;
import com.flight.booking.model.Flight;
import com.flight.booking.repository.BookingRepository;
import com.flight.booking.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private Flight sampleFlight;
    private ConcurrentHashMap<String, Flight> flightStore;

    @BeforeEach
    void setUp() {
        sampleFlight = Flight.builder()
                .flightNumber("FL001")
                .origin("New York")
                .destination("London")
                .departureTime(LocalDateTime.now().plusDays(1))
                .totalSeats(200)
                .availableSeats(200)
                .build();

        flightStore = new ConcurrentHashMap<>();
        flightStore.put("FL001", sampleFlight);
    }

    @Test
    void bookFlight_success_decrementsSeats() {
        BookingRequest request = new BookingRequest("FL001", List.of("John Doe", "Jane Doe"), "john@example.com");

        when(flightRepository.findByFlightNumber("FL001")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.getStore()).thenReturn(flightStore);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.bookFlight(request);

        assertThat(response).isNotNull();
        assertThat(response.flightNumber()).isEqualTo("FL001");
        assertThat(response.passengerNames()).containsExactly("John Doe", "Jane Doe");
        assertThat(response.numberOfSeats()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("Booking confirmed");
        assertThat(sampleFlight.getAvailableSeats()).isEqualTo(198);
    }

    @Test
    void bookFlight_insufficientSeats_throwsException() {
        sampleFlight.setAvailableSeats(1);
        BookingRequest request = new BookingRequest("FL001", List.of("P1", "P2", "P3", "P4", "P5"), "john@example.com");

        when(flightRepository.findByFlightNumber("FL001")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.getStore()).thenReturn(flightStore);

        assertThatThrownBy(() -> bookingService.bookFlight(request))
                .isInstanceOf(InsufficientSeatsException.class)
                .hasMessageContaining("Requested 5 seats but only 1 available");
    }

    @Test
    void bookFlight_flightNotFound_throwsException() {
        BookingRequest request = new BookingRequest("FL999", List.of("John Doe"), "john@example.com");

        when(flightRepository.findByFlightNumber("FL999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookFlight(request))
                .isInstanceOf(FlightNotFoundException.class)
                .hasMessageContaining("FL999");
    }

    @Test
    void bookFlight_concurrent_doesNotOversell() throws InterruptedException {
        sampleFlight.setAvailableSeats(5);

        when(flightRepository.findByFlightNumber("FL001")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.getStore()).thenReturn(flightStore);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    BookingRequest request = new BookingRequest("FL001", List.of("Passenger " + idx), "p" + idx + "@example.com");
                    bookingService.bookFlight(request);
                    successCount.incrementAndGet();
                } catch (InsufficientSeatsException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);
        assertThat(sampleFlight.getAvailableSeats()).isZero();
    }
}
