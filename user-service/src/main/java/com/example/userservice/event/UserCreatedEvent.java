package com.example.userservice.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreatedEvent {

    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
}