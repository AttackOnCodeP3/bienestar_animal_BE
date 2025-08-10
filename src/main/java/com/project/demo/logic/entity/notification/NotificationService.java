package com.project.demo.logic.entity.notification;

import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.logic.entity.complaint_state.ComplaintStateEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatus;
import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import com.project.demo.logic.entity.notification_type.NotificationType;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing notifications.
 * <p>
 * This service is responsible for handling notification-related operations.
 * It can be extended to include methods for creating, updating, and deleting notifications.
 * </p>
 *
 * @author dgutierrez
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationStatusRepository notificationStatusRepository;
    private final UserRepository userRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationService.class);

    /**
     * Sends a preconfigured notification to all users of a given municipality,
     * based on the selected NotificationTypeEnum.
     *
     * @param notificationType Type of notification to send (enum key).
     * @param actionUrl        Optional override for the action URL.
     * @param municipalityId   Municipality whose users will receive the notification.
     * @param excludedEmail    Email to exclude from notification (e.g., the creator).
     * @author dgutierrez
     */
    public void notifyAnnouncementCreationToMunicipalityUsers(
            NotificationTypeEnum notificationType,
            String actionUrl,
            Long municipalityId,
            String excludedEmail
    ) {
        var users = userRepository.findByMunicipality_IdAndEmailNot(municipalityId, excludedEmail);

        var type = notificationTypeRepository.findByName(notificationType.getName())
                .orElseThrow(() -> new IllegalStateException("Notification type '" + notificationType.getName() + "' not found"));

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Notification status '" + NotificationStatusEnum.SENT.getName() + "' not found"));

        var template = NotificationTemplateRegistry.getTemplate(notificationType);
        getResolvedActionUrl(actionUrl, users, type, status, template);
    }

    /**
     * Notifies all MUNICIPAL_ADMIN users associated with the given municipality
     * that a new complaint has been created.
     * <p>
     * This method retrieves all users with the role MUNICIPAL_ADMIN within the specified municipality,
     * builds a notification using a predefined template for complaint creation,
     * and saves it for each applicable user.
     * <p>
     * If no notification type or status is found in the system, the method throws an {@link IllegalStateException}.
     *
     * @param actionUrl optional URL that users can follow to view or act on the complaint;
     *                  if not provided, the default action URL from the template will be used.
     * @param municipalityId the ID of the municipality whose administrators should be notified.
     * @throws IllegalStateException if the notification type or status cannot be found in the system.
     * @author dgutierrez
     */
    public void notifyComplaintCreationToAdministrators(
            String actionUrl,
            Long municipalityId
    ) {
        var users = userRepository.findByMunicipalityIdAndRolesName(municipalityId, RoleEnum.MUNICIPAL_ADMIN);

        var type = notificationTypeRepository.findByName(NotificationTypeEnum.COMPLAINT.getName())
                .orElseThrow(() -> new IllegalStateException("No se encontró el tipo de notificación 'COMPLAINT_CREATED'"));

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("No se encontró el estado de notificación 'SENT'"));

        var template = NotificationTemplateRegistry.getTemplate(NotificationTypeEnum.COMPLAINT);
        getResolvedActionUrl(actionUrl, users, type, status, template);
    }

    private void getResolvedActionUrl(String actionUrl, List<User> users, NotificationType type, NotificationStatus status, NotificationTemplate template) {
        var resolvedActionUrl = (actionUrl != null && !actionUrl.isBlank()) ? actionUrl : template.actionUrl();

        var dateIssued = LocalDateTime.now();

        users.forEach(user -> {
            var notification = Notification.builder()
                    .title(template.title())
                    .description(template.message())
                    .actionUrl(resolvedActionUrl)
                    .dateIssued(dateIssued)
                    .user(user)
                    .notificationType(type)
                    .notificationStatus(status)
                    .build();

            notificationRepository.save(notification);
        });
    }

    /**
     * Notifies the user about a change in the state of their complaint.
     * <p>
     * This method retrieves the current state of the complaint and sends a notification
     * to the user who created the complaint, informing them of the state change.
     * </p>
     *
     * @param complaint The complaint whose state has changed.
     * @author dgutierrez
     */
    public void notifyComplaintStateChanged(Complaint complaint) {
        ComplaintStateEnum stateEnum = Arrays.stream(ComplaintStateEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(complaint.getComplaintState().getName()))
                .findFirst()
                .orElse(null);

        if (stateEnum == null) {
            logger.warn("No se pudo determinar el estado de la denuncia para notificar: {}", complaint.getComplaintState().getName());
            return;
        }

        Optional<NotificationType> typeOpt = notificationTypeRepository.findByName(NotificationTypeEnum.COMPLAINT.getName());
        if (typeOpt.isEmpty()) {
            logger.error("Tipo de notificación 'COMPLAINT' no encontrado");
            return;
        }

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Notification status '" + NotificationStatusEnum.SENT.getName() + "' not found"));

        NotificationType type = typeOpt.get();
        NotificationTemplate template = NotificationTemplateRegistry.getTemplate(NotificationTypeEnum.COMPLAINT);

        String stateMessage;
        switch (stateEnum) {
            case APPROVED -> stateMessage = "Su denuncia ha sido aprobada para seguimiento.";
            case WITH_OBSERVATIONS -> stateMessage = "Su denuncia requiere correcciones u observaciones.";
            case COMPLETED -> stateMessage = "Su denuncia ha sido atendida y marcada como completada.";
            default -> stateMessage = "El estado de su denuncia ha cambiado.";
        }

        Notification notification = Notification.builder()
                .title(template.title())
                .description(stateMessage)
                .notificationType(type)
                .dateIssued(LocalDateTime.now())
                .notificationStatus(status)
                .user(complaint.getCreatedBy())
                .build();

        notificationRepository.save(notification);
        logger.info("Notificación enviada al usuario {} por cambio de estado de denuncia a '{}'.", complaint.getCreatedBy().getEmail(), stateEnum.getName());
    }

    /**
     * Notifies the user about observations made on their complaint.
     * @param complaint The complaint that has been reviewed and has observations.
     * @author dgutierrez
     */
    public void notifyComplaintObservationsToUser(Complaint complaint) {
        var type = notificationTypeRepository.findByName(NotificationTypeEnum.COMPLAINT.getName());

        if (type.isEmpty()) {
            logger.error("Tipo de notificación 'COMPLAINT' no encontrado");
            throw new IllegalStateException("Tipo de notificación 'COMPLAINT' no encontrado");
        }

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Notification status '" + NotificationStatusEnum.SENT.getName() + "' not found"));

        var template = NotificationTemplateRegistry.getTemplate(NotificationTypeEnum.COMPLAINT);

        Notification notification = Notification.builder()
                .title("Observaciones a su denuncia")
                .description("Su denuncia ha sido revisada y tiene observaciones que deben ser corregidas.")
                .notificationType(type.get())
                .dateIssued(LocalDateTime.now())
                .notificationStatus(status)
                .user(complaint.getCreatedBy())
                .build();

        notificationRepository.save(notification);
        logger.info("Notificación de observaciones enviada a usuario {}", complaint.getCreatedBy().getEmail());
    }

    /**
     * Notifies municipal administrators that a complaint has been resubmitted by the user after corrections.
     * <p>
     * This method retrieves all municipal administrators for the municipality of the complaint,
     * builds a notification using a predefined template, and saves it for each administrator.
     * </p>
     *
     * @param complaint The complaint that has been resubmitted.
     * @author dgutierrez
     */
    public void notifyResubmission(Complaint complaint) {
        var admins = userRepository.findByMunicipalityIdAndRolesName(
                complaint.getCreatedBy().getMunicipality().getId(),
                RoleEnum.MUNICIPAL_ADMIN);

        var type = notificationTypeRepository.findByName(NotificationTypeEnum.COMPLAINT.getName())
                .orElseThrow(() -> new IllegalStateException("Tipo de notificación 'COMPLAINT' no encontrado"));

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Notification status '" + NotificationStatusEnum.SENT.getName() + "' not found"));

        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .title("Denuncia reenviada")
                    .description("Una denuncia ha sido reenviada por el usuario tras correcciones.")
                    .notificationType(type)
                    .dateIssued(LocalDateTime.now())
                    .notificationStatus(status)
                    .user(admin)
                    .build();

            notificationRepository.save(notification);
        }

        logger.info("Notificación de resubmisión enviada a administradores municipales");
    }

    /**
     * Notifies the user that their complaint has been completed.
     * <p>
     * This method builds a notification indicating that the user's complaint has been addressed
     * and is now considered complete, then saves it to the repository.
     * </p>
     *
     * @param complaint The complaint that has been completed.
     * @author dgutierrez
     */
    public void notifyComplaintCompleted(Complaint complaint) {
        var type = notificationTypeRepository.findByName(NotificationTypeEnum.COMPLAINT.getName())
                .orElseThrow(() -> new IllegalStateException("Tipo de notificación" + NotificationTypeEnum.COMPLAINT.getName() + " no encontrado"));

        var status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Notification status '" + NotificationStatusEnum.SENT.getName() + "' not found"));

        Notification notification = Notification.builder()
                .title("Denuncia completada")
                .description("Su denuncia ha sido atendida y se considera completada.")
                .notificationType(type)
                .dateIssued(LocalDateTime.now())
                .notificationStatus(status)
                .user(complaint.getCreatedBy())
                .build();

        notificationRepository.save(notification);
        logger.info("Notificación de denuncia completada enviada al usuario {}", complaint.getCreatedBy().getEmail());
    }
}
