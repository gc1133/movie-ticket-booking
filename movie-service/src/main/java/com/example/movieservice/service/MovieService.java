package com.example.movieservice.service;

import com.example.movieservice.dto.MovieDTO;
import com.example.movieservice.entity.Movie;
import com.example.movieservice.event.MovieAddedEvent;
import com.example.movieservice.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final KafkaTemplate<String, MovieAddedEvent> kafkaTemplate;

    @Autowired
    public MovieService(MovieRepository movieRepository, KafkaTemplate<String, MovieAddedEvent> kafkaTemplate) {
        this.movieRepository = movieRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        logger.info("Creating movie: {}", movieDTO.getTitle());

        Movie movie = new Movie();
        movie.setTitle(movieDTO.getTitle());
        movie.setLanguage(movieDTO.getLanguage());
        movie.setGenre(movieDTO.getGenre());
        movie = movieRepository.save(movie);

        MovieAddedEvent event = new MovieAddedEvent();
        event.setMovieId(movie.getId());
        event.setTitle(movie.getTitle());
        event.setLanguage(movie.getLanguage());
        event.setGenre(movie.getGenre());

        kafkaTemplate.executeInTransaction(ops -> {
            ops.send("movie-added", event);
            return true;
        });

        logger.info("Movie created with ID: {}", movie.getId());
        return mapToDTO(movie);
    }

    public List<MovieDTO> searchMovies(String title, String language, String genre) {
        logger.info("Searching movies with title: {}, language: {}, genre: {}", title, language, genre);

        List<Movie> movies;
        if (title != null) {
            movies = movieRepository.findByTitleContainingIgnoreCase(title);
        } else if (language != null) {
            movies = movieRepository.findByLanguage(language);
        } else if (genre != null) {
            movies = movieRepository.findByGenre(genre);
        } else {
            movies = movieRepository.findAll();
        }

        logger.info("Found {} movies", movies.size());
        return movies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private MovieDTO mapToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setLanguage(movie.getLanguage());
        dto.setGenre(movie.getGenre());
        return dto;
    }
}