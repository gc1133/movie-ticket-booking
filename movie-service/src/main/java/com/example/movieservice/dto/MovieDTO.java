package com.example.movieservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieDTO {

    private Long id;
    private String title;
    private String language;
    private String genre;
}