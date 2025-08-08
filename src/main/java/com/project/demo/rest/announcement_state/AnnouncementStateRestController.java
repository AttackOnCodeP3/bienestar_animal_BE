package com.project.demo.rest.announcement_state;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.announcement_state.AnnouncementState;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/announcement-states")
public class AnnouncementStateRestController {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementStateRestController.class);

    @Autowired
    private AnnouncementStateRepository announcementStateRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los estados de anuncio. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<AnnouncementState> statesPage = announcementStateRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, statesPage);

        return globalResponseHandler.handleResponse(
                "Estados de anuncio obtenidos correctamente",
                statesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo estado de anuncio con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<AnnouncementState> opt = announcementStateRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Estado de anuncio con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El estado de anuncio con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Estado de anuncio obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
