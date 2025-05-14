package com.example.movieservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Getter
@Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String language;

    private String description;

    private Integer duration;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    private String country;

    private String genre;
}