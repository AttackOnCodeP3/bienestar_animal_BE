package com.project.demo.rest.notification.dto;

import com.project.demo.logic.entity.notification.Notification;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record NotificationDTO(
        Long id,
        String title,
        String description,
        String imageUrl,
        LocalDate dateIssued,
        String actionUrl,
        String notificationStatusName,
        String notificationTypeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .description(notification.getDescription())
                .imageUrl(notification.getImageUrl())
                .dateIssued(notification.getDateIssued())
                .actionUrl(notification.getActionUrl())
                .notificationStatusName(notification.getNotificationStatus().getName())
                .notificationTypeName(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
