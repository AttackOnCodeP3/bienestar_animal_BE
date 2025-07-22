package com.project.demo.rest.race;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.race.RaceRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/races")
public class RaceRestController {

    private static final Logger logger = LoggerFactory.getLogger(RaceRestController.class);

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllRaces(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAllRaces - obteniendo todas las razas. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Race> racePage = raceRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, racePage);

        return globalResponseHandler.handleResponse(
                "Razas obtenidas correctamente",
                racePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRaceById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getRaceById - buscando raza con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Race> raceOpt = raceRepository.findById(id);
        if (raceOpt.isEmpty()) {
            logger.warn("Raza con ID {} no encontrada", id);
            return globalResponseHandler.notFound(
                    "La raza con ID " + id + " no fue encontrada",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Raza obtenida correctamente",
                raceOpt.get(),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/by-species/{speciesId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRacesBySpeciesId(
            @PathVariable Long speciesId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getRacesBySpeciesId - obteniendo razas para especie con ID: {}", speciesId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Species> speciesOpt = speciesRepository.findById(speciesId);
        if (speciesOpt.isEmpty()) {
            logger.warn("Especie con ID {} no encontrada", speciesId);
            return globalResponseHandler.notFound(
                    "La especie con ID " + speciesId + " no fue encontrada",
                    request
            );
        }

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Race> racePage = raceRepository.findAllBySpecies(speciesOpt.get(), pageable);

        Meta meta = PaginationUtils.buildMeta(request, racePage);

        return globalResponseHandler.handleResponse(
                "Razas de la especie obtenidas correctamente",
                racePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }
}
