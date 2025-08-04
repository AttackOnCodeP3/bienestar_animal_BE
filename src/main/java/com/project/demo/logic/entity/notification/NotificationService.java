package com.project.demo.logic.entity.notification;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
}
