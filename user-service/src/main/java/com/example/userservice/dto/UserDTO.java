package com.example.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}