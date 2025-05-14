package com.example.bookingservice.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeatsAllocatedEvent {

    private Long screenId;
    private List<Long> seatIds;
    private List<String> seatNumbers;
}