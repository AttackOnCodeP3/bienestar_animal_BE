package com.project.demo.rest.municipalityStatus;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipality.MunicipalityStatus;
import com.project.demo.logic.entity.municipality.MunicipalityStatusRepository;
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
@RequestMapping("/municipality-statuses")
public class MunicipalityStatusRestController {

    private static final Logger logger = LoggerFactory.getLogger(MunicipalityStatusRestController.class);

    @Autowired
    private MunicipalityStatusRepository municipalityStatusRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los estados de municipio. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MunicipalityStatus> statusPage = municipalityStatusRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, statusPage);

        return globalResponseHandler.handleResponse(
                "Estados de municipio obtenidos correctamente",
                statusPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getById - buscando estado de municipio con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            return globalResponseHandler.handleResponse(
                    "Estado de municipio obtenido correctamente",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Estado de municipio con ID {} no encontrado", id);
            return globalResponseHandler.notFound(
                    "El estado de municipio con ID " + id + " no fue encontrado",
                    request
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MunicipalityStatus status, HttpServletRequest request) {
        logger.info("Invocando create - creando nuevo estado de municipio: {}", status.getName());
        var globalResponseHandler = new GlobalResponseHandler();

        MunicipalityStatus saved = municipalityStatusRepository.save(status);
        return globalResponseHandler.created(
                "Estado de municipio creado correctamente",
                saved,
                request
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MunicipalityStatus status, HttpServletRequest request) {
        logger.info("Invocando update - actualizando estado de municipio con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            status.setId(id);
            MunicipalityStatus updated = municipalityStatusRepository.save(status);
            return globalResponseHandler.handleResponse(
                    "Estado de municipio actualizado correctamente",
                    updated,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Estado de municipio con ID {} no encontrado para actualización", id);
            return globalResponseHandler.notFound(
                    "El estado de municipio con ID " + id + " no fue encontrado",
                    request
            );
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody MunicipalityStatus status, HttpServletRequest request) {
        logger.info("Invocando patch - aplicando actualización parcial al estado de municipio con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            MunicipalityStatus existing = found.get();
            if (status.getName() != null) existing.setName(status.getName());
            if (status.getDescription() != null) existing.setDescription(status.getDescription());
            MunicipalityStatus patched = municipalityStatusRepository.save(existing);
            return globalResponseHandler.handleResponse(
                    "Estado de municipio actualizado correctamente",
                    patched,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Estado de municipio con ID {} no encontrado para parche", id);
            return globalResponseHandler.notFound(
                    "El estado de municipio con ID " + id + " no fue encontrado",
                    request
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando estado de municipio con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            municipalityStatusRepository.deleteById(id);
            return globalResponseHandler.handleResponse(
                    "Estado de municipio eliminado correctamente",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Estado de municipio con ID {} no encontrado para eliminación", id);
            return globalResponseHandler.notFound(
                    "El estado de municipio con ID " + id + " no fue encontrado",
                    request
            );
        }
    }
}
