package com.example.bookingservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingDTO {

    private Long id;
    private Long userId;
    private Long showId;
    private LocalDateTime bookingTime;
    private Double totalAmount;
    private String status;
    private List<Long> seatIds;
}