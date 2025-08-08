package com.project.demo.logic.entity.notification_status;

import lombok.Getter;

/**
 * Enum of possible statuses for a notification.
 * @author dgutierrez
 */
@Getter
public enum NotificationStatusEnum {
    SENT("Emitida", "La notificación ha sido generada y enviada."),
    READ("Leída", "El usuario ha abierto y visualizado la notificación."),
    ARCHIVED("Archivada", "La notificación ha sido marcada como no relevante o antigua por el usuario o el sistema.");

    private final String name;
    private final String description;

    NotificationStatusEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
