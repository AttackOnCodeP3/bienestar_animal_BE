package com.project.demo.rest.notification_type;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification_type.NotificationType;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
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
@RequestMapping("/notification-types")
public class NotificationTypeRestController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTypeRestController.class);

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los tipos de notificación. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<NotificationType> notificationTypesPage = notificationTypeRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, notificationTypesPage);

        return globalResponseHandler.handleResponse(
                "Tipos de notificación obtenidos correctamente",
                notificationTypesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo tipo de notificación con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<NotificationType> opt = notificationTypeRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Tipo de notificación con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El tipo de notificación con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Tipo de notificación obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
