package com.project.demo.rest.species;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/species")
public class SpeciesRestController {

    private static final Logger logger = LoggerFactory.getLogger(SpeciesRestController.class);

    @Autowired
    private SpeciesRepository speciesRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllSpecies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAllSpecies - obteniendo todas las especies. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Species> speciesPage = speciesRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, speciesPage);

        return globalResponseHandler.handleResponse(
                "Especies obtenidas correctamente",
                speciesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSpeciesById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getSpeciesById - obteniendo especie con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Species> speciesOpt = speciesRepository.findById(id);
        if (speciesOpt.isEmpty()) {
            logger.warn("Especie con ID {} no fue encontrada", id);
            return globalResponseHandler.notFound(
                    "La especie con ID " + id + " no fue encontrada",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Especie obtenida correctamente",
                speciesOpt.get(),
                HttpStatus.OK,
                request
        );
    }
}
