package com.project.demo.rest.notification_status;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification_status.NotificationStatus;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
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
@RequestMapping("/notification-statuses")
public class NotificationStatusRestController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStatusRestController.class);

    @Autowired
    private NotificationStatusRepository notificationStatusRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los estados de notificación. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<NotificationStatus> statusPage = notificationStatusRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, statusPage);

        return globalResponseHandler.handleResponse(
                "Estados de notificación obtenidos correctamente",
                statusPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo estado de notificación con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<NotificationStatus> opt = notificationStatusRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Estado de notificación con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El estado de notificación con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Estado de notificación obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
