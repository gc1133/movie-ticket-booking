package com.example.bookingservice.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingCreatedEvent {

    private Long bookingId;
    private Long userId;
    private Long showId;
    private LocalDateTime bookingTime;
    private Double totalAmount;
    private List<Long> seatIds;
}