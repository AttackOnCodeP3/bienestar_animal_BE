package com.project.demo.rest.neighborhood;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/neighborhoods")
public class NeighborhoodRestController {

    private static final Logger logger = LoggerFactory.getLogger(NeighborhoodRestController.class);

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los barrios. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Neighborhood> neighborhoodPage = neighborhoodRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, neighborhoodPage);

        return globalResponseHandler.handleResponse(
                "Barrios obtenidos correctamente",
                neighborhoodPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{neighborhoodId}")
    public ResponseEntity<?> getById(@PathVariable Long neighborhoodId, HttpServletRequest request) {
        logger.info("Invocando getById - buscando barrio con ID: {}", neighborhoodId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(neighborhoodId);
        if (neighborhood.isPresent()) {
            return globalResponseHandler.handleResponse(
                    "Barrio obtenido correctamente",
                    neighborhood.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Barrio con ID {} no encontrado", neighborhoodId);
            return globalResponseHandler.notFound(
                    "El barrio con ID " + neighborhoodId + " no fue encontrado",
                    request
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        logger.info("Invocando create - creando nuevo barrio: {}", neighborhood.getName());
        var globalResponseHandler = new GlobalResponseHandler();

        Neighborhood saved = neighborhoodRepository.save(neighborhood);
        return globalResponseHandler.created(
                "Barrio creado correctamente",
                saved,
                request
        );
    }

    @PutMapping("/{neighborhoodId}")
    public ResponseEntity<?> update(@PathVariable Long neighborhoodId, @RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        logger.info("Invocando update - actualizando barrio con ID: {}", neighborhoodId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            neighborhood.setId(neighborhoodId);
            Neighborhood updated = neighborhoodRepository.save(neighborhood);
            return globalResponseHandler.handleResponse(
                    "Barrio actualizado correctamente",
                    updated,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Barrio con ID {} no encontrado para actualización", neighborhoodId);
            return globalResponseHandler.notFound(
                    "El barrio con ID " + neighborhoodId + " no fue encontrado",
                    request
            );
        }
    }

    @PatchMapping("/{neighborhoodId}")
    public ResponseEntity<?> patch(@PathVariable Long neighborhoodId, @RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        logger.info("Invocando patch - aplicando actualización parcial al barrio con ID: {}", neighborhoodId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            Neighborhood existing = found.get();
            if (neighborhood.getName() != null) existing.setName(neighborhood.getName());
            if (neighborhood.getDistrict() != null) existing.setDistrict(neighborhood.getDistrict());
            neighborhoodRepository.save(existing);
            return globalResponseHandler.handleResponse(
                    "Barrio actualizado correctamente",
                    existing,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Barrio con ID {} no encontrado para parche", neighborhoodId);
            return globalResponseHandler.notFound(
                    "El barrio con ID " + neighborhoodId + " no fue encontrado",
                    request
            );
        }
    }

    @DeleteMapping("/{neighborhoodId}")
    public ResponseEntity<?> delete(@PathVariable Long neighborhoodId, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando barrio con ID: {}", neighborhoodId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            neighborhoodRepository.deleteById(neighborhoodId);
            return globalResponseHandler.handleResponse(
                    "Barrio eliminado correctamente",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Barrio con ID {} no encontrado para eliminación", neighborhoodId);
            return globalResponseHandler.notFound(
                    "El barrio con ID " + neighborhoodId + " no fue encontrado",
                    request
            );
        }
    }
}
