package com.flight.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.booking.dto.BookingRequest;
import com.flight.booking.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FlightBookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        BookingRequest request = new BookingRequest("FL001", List.of("Jane Doe", "John Doe"), "jane@example.com");

        mockMvc.perform(post("/flight/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.flightNumber").value("FL001"))
                .andExpect(jsonPath("$.passengerNames[0]").value("Jane Doe"))
                .andExpect(jsonPath("$.passengerNames[1]").value("John Doe"))
                .andExpect(jsonPath("$.numberOfSeats").value(2))
                .andExpect(jsonPath("$.status").value("Booking confirmed"))
                .andExpect(jsonPath("$.bookingId").isNotEmpty());
    }

    @Test
    void createBooking_missingFields_returns400() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/flight/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void createBooking_unknownFlight_returns404() throws Exception {
        BookingRequest request = new BookingRequest("FL999", List.of("Jane Doe"), "jane@example.com");

        mockMvc.perform(post("/flight/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Flight Not Found"));
    }

    @Test
    void createBooking_seatsExhausted_returns409() throws Exception {
        int totalSeats = flightRepository.findByFlightNumber("FL002").orElseThrow().getTotalSeats();

        List<String> passengers = new ArrayList<>();
        for (int i = 0; i <= totalSeats; i++) {
            passengers.add("Passenger " + i);
        }
        BookingRequest request = new BookingRequest("FL002", passengers, "jane@example.com");

        mockMvc.perform(post("/flight/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Insufficient Seats"));
    }
}
