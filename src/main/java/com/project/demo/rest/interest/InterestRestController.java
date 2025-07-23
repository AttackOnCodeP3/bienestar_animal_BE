package com.project.demo.rest.interest;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
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
@RequestMapping("/interests")
public class InterestRestController {

    private static final Logger logger = LoggerFactory.getLogger(InterestRestController.class);

    @Autowired
    private InterestRepository interestRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los intereses. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Interest> interestPage = interestRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, interestPage);

        return globalResponseHandler.handleResponse(
                "Intereses obtenidos correctamente",
                interestPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{interestId}")
    public ResponseEntity<?> getById(@PathVariable Long interestId, HttpServletRequest request) {
        logger.info("Invocando getById - buscando interés con ID: {}", interestId);
        Optional<Interest> interest = interestRepository.findById(interestId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (interest.isPresent()) {
            return globalResponseHandler.handleResponse(
                    "Interés obtenido correctamente",
                    interest.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Interés con ID {} no fue encontrado", interestId);
            return globalResponseHandler.notFound(
                    "El interés con ID " + interestId + " no fue encontrado",
                    request
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Interest interest, HttpServletRequest request) {
        logger.info("Invocando create - creando interés: {}", interest.getName());
        var globalResponseHandler = new GlobalResponseHandler();
        Interest saved = interestRepository.save(interest);
        return globalResponseHandler.created(
                "Interés creado correctamente",
                saved,
                request
        );
    }

    @PutMapping("/{interestId}")
    public ResponseEntity<?> update(@PathVariable Long interestId, @RequestBody Interest interest, HttpServletRequest request) {
        logger.info("Invocando update - actualizando interés con ID: {}", interestId);
        Optional<Interest> found = interestRepository.findById(interestId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            interest.setId(interestId);
            Interest updated = interestRepository.save(interest);
            return globalResponseHandler.success(
                    "Interés actualizado correctamente",
                    updated,
                    request
            );
        } else {
            logger.warn("Interés con ID {} no fue encontrado para actualizar", interestId);
            return globalResponseHandler.notFound(
                    "El interés con ID " + interestId + " no fue encontrado",
                    request
            );
        }
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<?> patch(@PathVariable Long interestId, @RequestBody Interest interest, HttpServletRequest request) {
        logger.info("Invocando patch - actualizando parcialmente interés con ID: {}", interestId);
        Optional<Interest> found = interestRepository.findById(interestId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            Interest existing = found.get();
            if (interest.getName() != null) existing.setName(interest.getName());
            if (interest.getDescription() != null) existing.setDescription(interest.getDescription());
            Interest patched = interestRepository.save(existing);
            return globalResponseHandler.success(
                    "Interés actualizado correctamente",
                    patched,
                    request
            );
        } else {
            logger.warn("Interés con ID {} no fue encontrado para parche", interestId);
            return globalResponseHandler.notFound(
                    "El interés con ID " + interestId + " no fue encontrado",
                    request
            );
        }
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<?> delete(@PathVariable Long interestId, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando interés con ID: {}", interestId);
        Optional<Interest> found = interestRepository.findById(interestId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            interestRepository.deleteById(interestId);
            return globalResponseHandler.success(
                    "Interés eliminado correctamente",
                    found.get(),
                    request
            );
        } else {
            logger.warn("Interés con ID {} no fue encontrado para eliminación", interestId);
            return globalResponseHandler.notFound(
                    "El interés con ID " + interestId + " no fue encontrado",
                    request
            );
        }
    }
}
