package com.example.notificationservice.service;

import com.example.notificationservice.event.BookingConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

@SpringBootTest
@Testcontainers
class NotificationServiceTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    @Test
    void testHandleBookingConfirmedEvent() {
        BookingConfirmedEvent event = new BookingConfirmedEvent();
        event.setBookingId(1L);
        event.setUserId(1L);
        event.setUserEmail("john@example.com");
        event.setShowId(1L);
        event.setBookingTime(LocalDateTime.now());
        event.setTotalAmount(20.00);

        kafkaTemplate.send("booking-confirmed", event);

        // Add delay or use Awaitility to wait for Kafka consumer
        try {
            Thread.sleep(2000); // Wait for consumer to process
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify via logs or mock (simplified for demo)
    }
}