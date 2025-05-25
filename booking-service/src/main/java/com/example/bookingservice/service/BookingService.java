package com.example.bookingservice.service;

import com.example.bookingservice.client.UserClient;
import com.example.bookingservice.dto.BookingDTO;
import com.example.bookingservice.dto.UserDTO;
import com.example.bookingservice.entity.BookedSeat;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.Payment;
import com.example.bookingservice.event.BookingConfirmedEvent;
import com.example.bookingservice.event.BookingCreatedEvent;
import com.example.bookingservice.event.SeatsAllocatedEvent;
import com.example.bookingservice.event.ShowCreatedEvent;
import com.example.bookingservice.exception.BookingException;
import com.example.bookingservice.repository.BookedSeatRepository;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final PaymentRepository paymentRepository;
    private final UserClient userClient;
    private final KafkaTemplate<String, BookingCreatedEvent> bookingKafkaTemplate;
    private final KafkaTemplate<String, BookingConfirmedEvent> confirmKafkaTemplate;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            BookedSeatRepository bookedSeatRepository,
            PaymentRepository paymentRepository,
            UserClient userClient,
            KafkaTemplate<String, BookingCreatedEvent> bookingKafkaTemplate,
            KafkaTemplate<String, BookingConfirmedEvent> confirmKafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.paymentRepository = paymentRepository;
        this.userClient = userClient;
        this.bookingKafkaTemplate = bookingKafkaTemplate;
        this.confirmKafkaTemplate = confirmKafkaTemplate;
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        logger.info("Creating booking for user ID: {}, show ID: {}", bookingDTO.getUserId(), bookingDTO.getShowId());

        UserDTO user = userClient.getUser(bookingDTO.getUserId());
        if (user == null) {
            throw new BookingException("User not found with ID: " + bookingDTO.getUserId());
        }

        Booking booking = new Booking();
        booking.setUserId(bookingDTO.getUserId());
        booking.setShowId(bookingDTO.getShowId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(bookingDTO.getTotalAmount());
        booking.setStatus(Booking.Status.PENDING);
        booking = bookingRepository.save(booking);

        List<BookedSeat> bookedSeats = new ArrayList<>();
        for (Long seatId : bookingDTO.getSeatIds()) {
            BookedSeat bookedSeat = new BookedSeat();
            bookedSeat.setBooking(booking);
            bookedSeat.setSeatId(seatId);
            bookedSeats.add(bookedSeat);
        }
        bookedSeatRepository.saveAll(bookedSeats);
        booking.setBookedSeats(bookedSeats);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(bookingDTO.getTotalAmount());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus(Payment.Status.PENDING);
        paymentRepository.save(payment);
        booking.setPayment(payment);

        BookingCreatedEvent event = new BookingCreatedEvent();
        event.setBookingId(booking.getId());
        event.setUserId(booking.getUserId());
        event.setShowId(booking.getShowId());
        event.setBookingTime(booking.getBookingTime());
        event.setTotalAmount(booking.getTotalAmount());
        event.setSeatIds(bookingDTO.getSeatIds());

        bookingKafkaTemplate.executeInTransaction(ops -> {
            ops.send("booking-created", event);
            return true;
        });

        logger.info("Booking created with ID: {}", booking.getId());
        return mapToDTO(booking);
    }

    @KafkaListener(topics = "show-created", groupId = "booking-group")
    public void handleShowCreatedEvent(ShowCreatedEvent event) {
        logger.info("Received show created event for show ID: {}", event.getShowId());
        // Logic to handle show creation, e.g., validate or cache show details
    }

    @KafkaListener(topics = "seats-allocated", groupId = "booking-group")
    public void handleSeatsAllocatedEvent(SeatsAllocatedEvent event) {
        logger.info("Received seats allocated event for screen ID: {}", event.getScreenId());
        // Logic to validate or cache seat allocations
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        logger.info("Confirming booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found with ID: " + bookingId));

        booking.setStatus(Booking.Status.CONFIRMED);
        Payment payment = booking.getPayment();
        payment.setStatus(Payment.Status.COMPLETED);
        bookingRepository.save(booking);
        paymentRepository.save(payment);

        UserDTO user = userClient.getUser(booking.getUserId());
        BookingConfirmedEvent event = new BookingConfirmedEvent();
        event.setBookingId(booking.getId());
        event.setUserId(booking.getUserId());
        event.setUserEmail(user.getEmail());
        event.setShowId(booking.getShowId());
        event.setBookingTime(booking.getBookingTime());
        event.setTotalAmount(booking.getTotalAmount());

        confirmKafkaTemplate.executeInTransaction(ops -> {
            ops.send("booking-confirmed", event);
            return true;
        });

        logger.info("Booking confirmed with ID: {}", booking.getId());
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setShowId(booking.getShowId());
        dto.setBookingTime(booking.getBookingTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus().name());
        dto.setSeatIds(booking.getBookedSeats().stream()
                .map(BookedSeat::getSeatId)
                .collect(Collectors.toList()));
        return dto;
    }

}