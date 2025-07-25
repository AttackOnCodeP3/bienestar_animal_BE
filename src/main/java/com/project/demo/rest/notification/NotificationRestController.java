package com.project.demo.rest.notification;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification.Notification;
import com.project.demo.logic.entity.notification.NotificationRepository;
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
@RequestMapping("/notifications")
public class NotificationRestController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRestController.class);

    @Autowired private NotificationRepository notificationRepository;

    @Autowired private JwtService jwtService;

    @GetMapping("/my-notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getMyNotifications - Obteniendo mis notificaciones. Page: {}, Size: {}", page, size);
        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notificationPage = notificationRepository.findByUser_Email(email, pageable);
        Meta meta = PaginationUtils.buildMeta(request, notificationPage);
        return new GlobalResponseHandler().handleResponse(
                "Mis notificaciones obtenidas correctamente",
                notificationPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long notificationId, HttpServletRequest request) {
        logger.info("Invocando getById - Obteniendo notificación con ID: {}", notificationId);
        Optional<Notification> optional = notificationRepository.findById(notificationId);

        if (optional.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Notificación obtenida correctamente",
                    optional.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().notFound(
                    "No se encontró la notificación con ID " + notificationId,
                    request
            );
        }
    }
}
