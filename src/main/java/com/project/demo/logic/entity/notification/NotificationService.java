package com.project.demo.logic.entity.notification;

import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
     * Sends a notification to all users of a given municipality when a new announcement is created.
     *
     * @param announcementTitle   Title of the created announcement.
     * @param announcementSummary Description or summary of the announcement.
     * @param actionUrl           Optional URL the user can click for details.
     * @param municipalityId      Municipality ID whose users will be notified.
     * @param excludedEmail       Email to exclude from notifications (e.g., the creator of the announcement).
     * @author dgutierrez
     */
    public void notifyAnnouncementCreationToMunicipalityUsers(
            String announcementTitle,
            String announcementSummary,
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

        var dateIssued = LocalDate.now();

        users.forEach(user -> {
            var notif = Notification.builder()
                    .title("Nuevo anuncio: " + announcementTitle)
                    .description(announcementSummary)
                    .actionUrl(actionUrl)
                    .dateIssued(dateIssued)
                    .user(user)
                    .notificationType(type)
                    .notificationStatus(status)
                    .build();

            notificationRepository.save(notif);
        });
    }
}
