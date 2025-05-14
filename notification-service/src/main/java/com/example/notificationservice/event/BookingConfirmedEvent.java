package com.example.notificationservice.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingConfirmedEvent {

    private Long bookingId;
    private Long userId;
    private String userEmail;
    private Long showId;
    private LocalDateTime bookingTime;
    private Double totalAmount;
}