package com.example.notificationservice.service;

import com.example.notificationservice.event.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = "booking-confirmed", groupId = "notification-group")
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        logger.info("Received booking confirmed event for booking ID: {}", event.getBookingId());
        // Simulate sending notification (e.g., email or SMS)
        logger.info("Sending notification to user: {} for booking ID: {}, amount: {}",
                event.getUserEmail(), event.getBookingId(), event.getTotalAmount());
    }
}