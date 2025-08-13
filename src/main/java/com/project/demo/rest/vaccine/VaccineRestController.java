package com.project.demo.rest.vaccine;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.vaccine.Vaccine;
import com.project.demo.logic.entity.vaccine.VaccineRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/vaccines")
public class VaccineRestController {

    private static final Logger logger = LoggerFactory.getLogger(VaccineRestController.class);

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllVaccines(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAllVaccines - obteniendo todas las vacunas. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Vaccine> vaccinePage = vaccineRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, vaccinePage);

        return globalResponseHandler.handleResponse(
                "Vacunas obtenidas correctamente",
                vaccinePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getVaccineById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getVaccineById - obteniendo vacuna con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Vaccine> vaccineOpt = vaccineRepository.findById(id);
        if (vaccineOpt.isEmpty()) {
            logger.warn("Vacuna con ID {} no fue encontrada", id);
            return globalResponseHandler.notFound(
                    "La vacuna con ID " + id + " no fue encontrada",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Vacuna obtenida correctamente",
                vaccineOpt.get(),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/by-species/{speciesId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getVaccinesBySpeciesId(
            @PathVariable Long speciesId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getVaccinesBySpeciesId - obteniendo vacunas para especie con ID: {}", speciesId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Species> speciesOpt = speciesRepository.findById(speciesId);
        if (speciesOpt.isEmpty()) {
            logger.warn("Especie con ID {} no fue encontrada", speciesId);
            return globalResponseHandler.notFound(
                    "La especie con ID " + speciesId + " no fue encontrada",
                    request
            );
        }

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Vaccine> vaccinePage = vaccineRepository.findAllBySpeciesId(speciesId, pageable);

        Meta meta = PaginationUtils.buildMeta(request, vaccinePage);

        return globalResponseHandler.handleResponse(
                "Vacunas para la especie obtenidas correctamente",
                vaccinePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }
}