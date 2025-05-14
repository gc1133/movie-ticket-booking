package com.example.theatreservice.service;

import com.example.theatreservice.dto.ShowDTO;
import com.example.theatreservice.dto.TheatreDTO;
import com.example.theatreservice.entity.*;
import com.example.theatreservice.event.SeatsAllocatedEvent;
import com.example.theatreservice.event.ShowCreatedEvent;
import com.example.theatreservice.event.TheatreCreatedEvent;
import com.example.theatreservice.exception.ResourceNotFoundException;
import com.example.theatreservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheatreService {

    private static final Logger logger = LoggerFactory.getLogger(TheatreService.class);

    private final TheatreRepository theatreRepository;
    private final CityRepository cityRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final KafkaTemplate<String, TheatreCreatedEvent> theatreKafkaTemplate;
    private final KafkaTemplate<String, ShowCreatedEvent> showKafkaTemplate;
    private final KafkaTemplate<String, SeatsAllocatedEvent> seatKafkaTemplate;

    @Autowired
    public TheatreService(
            TheatreRepository theatreRepository,
            CityRepository cityRepository,
            ScreenRepository screenRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository,
            KafkaTemplate<String, TheatreCreatedEvent> theatreKafkaTemplate,
            KafkaTemplate<String, ShowCreatedEvent> showKafkaTemplate,
            KafkaTemplate<String, SeatsAllocatedEvent> seatKafkaTemplate) {
        this.theatreRepository = theatreRepository;
        this.cityRepository = cityRepository;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.theatreKafkaTemplate = theatreKafkaTemplate;
        this.showKafkaTemplate = showKafkaTemplate;
        this.seatKafkaTemplate = seatKafkaTemplate;
    }

    @Transactional
    public TheatreDTO createTheatre(TheatreDTO theatreDTO) {
        logger.info("Creating theatre: {}", theatreDTO.getName());

        City city = cityRepository.findById(theatreDTO.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + theatreDTO.getCityId()));

        Theatre theatre = new Theatre();
        theatre.setName(theatreDTO.getName());
        theatre.setCity(city);
        theatre = theatreRepository.save(theatre);

        TheatreCreatedEvent event = new TheatreCreatedEvent();
        event.setTheatreId(theatre.getId());
        event.setName(theatre.getName());
        event.setCityId(theatre.getCity().getId());

        theatreKafkaTemplate.executeInTransaction(ops -> {
            ops.send("theatre-created", event);
            return true;
        });

        logger.info("Theatre created with ID: {}", theatre.getId());
        return mapToDTO(theatre);
    }

    @Transactional
    public ShowDTO createShow(Long theatreId, ShowDTO showDTO) {
        logger.info("Creating show for theatre ID: {}", theatreId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        Screen screen = screenRepository.findById(showDTO.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + showDTO.getScreenId()));

        Show show = new Show();
        show.setMovieId(showDTO.getMovieId());
        show.setScreen(screen);
        show.setStartTime(showDTO.getStartTime());
        show.setEndTime(showDTO.getEndTime());
        show.setStatus(Show.Status.valueOf(showDTO.getStatus()));
        show = showRepository.save(show);

        ShowCreatedEvent event = new ShowCreatedEvent();
        event.setShowId(show.getId());
        event.setMovieId(show.getMovieId());
        event.setScreenId(show.getScreen().getId());
        event.setStartTime(show.getStartTime());
        event.setEndTime(show.getEndTime());

        showKafkaTemplate.executeInTransaction(ops -> {
            ops.send("show-created", event);
            return true;
        });

        logger.info("Show created with ID: {}", show.getId());
        return mapToShowDTO(show);
    }

    @Transactional
    public List<Long> allocateSeats(Long theatreId, Long screenId, List<String> seatNumbers) {
        logger.info("Allocating seats for theatre ID: {}, screen ID: {}", theatreId, screenId);

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + screenId));

        List<Seat> seats = new ArrayList<>();
        List<Long> seatIds = new ArrayList<>();
        List<String> allocatedSeatNumbers = new ArrayList<>();

        for (String seatNumber : seatNumbers) {
            Seat seat = new Seat();
            seat.setScreen(screen);
            seat.setSeatNumber(seatNumber);
            seats.add(seat);
        }

        seats = seatRepository.saveAll(seats);

        for (Seat seat : seats) {
            seatIds.add(seat.getId());
            allocatedSeatNumbers.add(seat.getSeatNumber());
        }

        SeatsAllocatedEvent event = new SeatsAllocatedEvent();
        event.setScreenId(screenId);
        event.setSeatIds(seatIds);
        event.setSeatNumbers(allocatedSeatNumbers);

        seatKafkaTemplate.executeInTransaction(ops -> {
            ops.send("seats-allocated", event);
            return true;
        });

        logger.info("Allocated {} seats for screen ID: {}", seatIds.size(), screenId);
        return seatIds;
    }

    private TheatreDTO mapToDTO(Theatre theatre) {
        TheatreDTO dto = new TheatreDTO();
        dto.setId(theatre.getId());
        dto.setName(theatre.getName());
        dto.setCityId(theatre.getCity().getId());
        return dto;
    }

    private ShowDTO mapToShowDTO(Show show) {
        ShowDTO dto = new ShowDTO();
        dto.setId(show.getId());
        dto.setMovieId(show.getMovieId());
        dto.setScreenId(show.getScreen().getId());
        dto.setStartTime(show.getStartTime());
        dto.setEndTime(show.getEndTime());
        dto.setStatus(show.getStatus().name());
        return dto;
    }
}