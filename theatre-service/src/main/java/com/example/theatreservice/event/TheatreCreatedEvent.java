package com.example.theatreservice.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TheatreCreatedEvent {

    private Long theatreId;
    private String name;
    private Long cityId;
}