package com.example.movieservice.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieAddedEvent {

    private Long movieId;
    private String title;
    private String language;
    private String genre;
}