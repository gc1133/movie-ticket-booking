package com.example.theatreservice.controller;

import com.example.theatreservice.dto.ShowDTO;
import com.example.theatreservice.dto.TheatreDTO;
import com.example.theatreservice.service.TheatreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private static final Logger logger = LoggerFactory.getLogger(TheatreController.class);

    private final TheatreService theatreService;

    @Autowired
    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    public ResponseEntity<TheatreDTO> createTheatre(@RequestBody TheatreDTO theatreDTO) {
        logger.info("Received request to create theatre: {}", theatreDTO.getName());
        TheatreDTO createdTheatre = theatreService.createTheatre(theatreDTO);
        return ResponseEntity.ok(createdTheatre);
    }

    @PostMapping("/{theatreId}/shows")
    public ResponseEntity<ShowDTO> createShow(@PathVariable Long theatreId, @RequestBody ShowDTO showDTO) {
        logger.info("Received request to create show for theatre ID: {}", theatreId);
        ShowDTO createdShow = theatreService.createShow(theatreId, showDTO);
        return ResponseEntity.ok(createdShow);
    }

    @PostMapping("/{theatreId}/screens/{screenId}/seats")
    public ResponseEntity<List<Long>> allocateSeats(
            @PathVariable Long theatreId,
            @PathVariable Long screenId,
            @RequestBody List<String> seatNumbers) {
        logger.info("Received request to allocate seats for theatre ID: {}, screen ID: {}", theatreId, screenId);
        List<Long> seatIds = theatreService.allocateSeats(theatreId, screenId, seatNumbers);
        return ResponseEntity.ok(seatIds);
    }
}