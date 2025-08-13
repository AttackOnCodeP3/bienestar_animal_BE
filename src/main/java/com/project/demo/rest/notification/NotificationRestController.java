package com.project.demo.rest.notification;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification.Notification;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import com.project.demo.rest.notification.dto.NotificationDTO;
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
    @Autowired private NotificationStatusRepository notificationStatusRepository;
    @Autowired private JwtService jwtService;

    /**
     * @author dgutierrez
     */
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
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage = notificationRepository.findByUser_Email(email, pageable);
        Page<NotificationDTO> dtoPage = notificationPage.map(NotificationDTO::fromEntity);

        Meta meta = PaginationUtils.buildMeta(request, dtoPage);

        return new GlobalResponseHandler().handleResponse(
                "Mis notificaciones obtenidas correctamente",
                dtoPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * @author dgutierrez
     */
    @GetMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long notificationId, HttpServletRequest request) {
        logger.info("Invocando getById - Obteniendo notificación con ID: {}", notificationId);
        Optional<Notification> optional = notificationRepository.findById(notificationId);

        if (optional.isPresent()) {
            NotificationDTO dto = NotificationDTO.fromEntity(optional.get());

            return new GlobalResponseHandler().handleResponse(
                    "Notificación obtenida correctamente",
                    dto,
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

    /**
     * @author dgutierrez
     */
    @GetMapping("/count-my-notifications-by-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> countMyNotificationsByStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("statusId") Long statusId,
            HttpServletRequest request
    ) {
        logger.info("Invocando countMyNotificationsByStatus - Status ID: {}", statusId);
        var globalResponseHandler = new GlobalResponseHandler();

        boolean exists = notificationStatusRepository.existsById(statusId);
        if (!exists) {
            logger.warn("Estado con ID {} no encontrado en la base de datos", statusId);

            var availableStatuses = notificationStatusRepository.findAll().stream()
                    .map(status -> String.format("[id: %d, name: %s]", status.getId(), status.getName()))
                    .toList();

            return globalResponseHandler.badRequest(
                    "El ID de estado proporcionado no es válido. Estados válidos: " + availableStatuses,
                    request
            );
        }

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        long count = notificationRepository.countByUser_EmailAndNotificationStatus_Id(email, statusId);

        return globalResponseHandler.handleResponse(
                "Conteo de notificaciones del usuario por estado obtenido correctamente",
                count,
                HttpStatus.OK,
                request
        );
    }

    /**
     * @author dgutierrez
     */
    @GetMapping("/count-by-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> countByStatus(
            @RequestParam("statusId") Long statusId,
            HttpServletRequest request
    ) {
        logger.info("Invocando countByStatus - Solicitado estado con ID: {}", statusId);
        var globalResponseHandler = new GlobalResponseHandler();

        boolean exists = notificationStatusRepository.existsById(statusId);
        if (!exists) {
            logger.warn("Estado con ID {} no encontrado en la base de datos", statusId);

            var availableStatuses = notificationStatusRepository.findAll().stream()
                    .map(status -> String.format("[id: %d, name: %s]", status.getId(), status.getName()))
                    .toList();

            return globalResponseHandler.badRequest(
                    "El ID de estado proporcionado no es válido. Estados válidos: " + availableStatuses,
                    request
            );
        }

        long count = notificationRepository.countByNotificationStatus_Id(statusId);

        return globalResponseHandler.handleResponse(
                "Conteo de notificaciones por estado obtenido correctamente",
                count,
                HttpStatus.OK,
                request
        );
    }

    /**
     * @author dgutierrez
     */
    @PutMapping("/mark-my-notifications-as-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markMyNotificationsAsRead(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request
    ) {
        logger.info("Invocando markMyNotificationsAsRead - marcando como leídas todas las notificaciones del usuario autenticado");

        var globalResponseHandler = new GlobalResponseHandler();

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));

        var readStatusOpt = notificationStatusRepository.findByName(NotificationStatusEnum.READ.getName());

        if (readStatusOpt.isEmpty()) {
            return globalResponseHandler.internalError(
                    "El estado 'READ' no se encuentra configurado en la base de datos.",
                    request
            );
        }

        var readStatus = readStatusOpt.get();

        var updatedCount = notificationRepository.markAllAsReadForUser(email, readStatus.getId());

        logger.info("Se marcaron {} notificaciones como leídas para el usuario {}", updatedCount, email);

        return globalResponseHandler.handleResponse(
                "Notificaciones marcadas como leídas correctamente",
                updatedCount,
                HttpStatus.OK,
                request
        );
    }
}
