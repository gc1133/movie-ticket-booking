package com.example.movieservice.service;

import com.example.movieservice.dto.MovieDTO;
import com.example.movieservice.entity.Movie;
import com.example.movieservice.event.MovieAddedEvent;
import com.example.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class MovieServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("moviedb")
            .withUsername("admin")
            .withPassword("password");

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @MockBean
    private KafkaTemplate<String, MovieAddedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();
    }

    @Test
    void testCreateMovie() {
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Sample Movie");
        movieDTO.setLanguage("English");
        movieDTO.setGenre("Action");

        when(kafkaTemplate.executeInTransaction(any())).thenReturn(true);

        MovieDTO result = movieService.createMovie(movieDTO);

        assertNotNull(result.getId());
        assertEquals("Sample Movie", result.getTitle());
        verify(kafkaTemplate, times(1)).executeInTransaction(any());
    }

    @Test
    void testSearchMoviesByTitle() {
        Movie movie = new Movie();
        movie.setTitle("Sample Movie");
        movie.setLanguage("English");
        movie.setGenre("Action");
        movieRepository.save(movie);

        List<MovieDTO> result = movieService.searchMovies("Sample", null, null);

        assertEquals(1, result.size());
        assertEquals("Sample Movie", result.get(0).getTitle());
    }
}