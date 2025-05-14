package com.example.bookingservice.service;

import com.example.bookingservice.client.UserClient;
import com.example.bookingservice.dto.BookingDTO;
import com.example.bookingservice.dto.UserDTO;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.event.BookingCreatedEvent;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.BookedSeatRepository;
import com.example.bookingservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class BookingServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("bookingdb")
            .withUsername("admin")
            .withPassword("password");

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookedSeatRepository bookedSeatRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private UserClient userClient;

    @MockBean
    private KafkaTemplate<String, BookingCreatedEvent> bookingKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, Object> confirmKafkaTemplate;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        bookedSeatRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void testCreateBooking() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("John Doe");
        userDTO.setEmail("john@example.com");

        when(userClient.getUser(1L)).thenReturn(userDTO);
        when(bookingKafkaTemplate.executeInTransaction(any())).thenReturn(true);

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setUserId(1L);
        bookingDTO.setShowId(1L);
        bookingDTO.setTotalAmount(20.00);
        bookingDTO.setSeatIds(Arrays.asList(1L, 2L));

        BookingDTO result = bookingService.createBooking(bookingDTO);

        assertNotNull(result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(2, result.getSeatIds().size());
        assertEquals("PENDING", result.getStatus());
        verify(bookingKafkaTemplate, times(1)).executeInTransaction(any());
    }
}