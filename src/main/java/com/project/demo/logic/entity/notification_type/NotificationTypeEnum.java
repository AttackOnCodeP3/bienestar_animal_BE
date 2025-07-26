package com.project.demo.logic.entity.notification_type;

import lombok.Getter;

/**
 * Enum of possible types for a notification.
 * @author dgutierrez
 */
@Getter
public enum NotificationTypeEnum {
    CAMPAIGN("Campaña", "Notificaciones relacionadas con campañas municipales (vacunación, adopción)."),
    SANITARY_ALERT("Alerta Sanitaria", "Recordatorios o alertas sobre controles sanitarios (desparasitación, vacunas)."),
    ADOPTION("Adopción", "Actualizaciones o nuevas oportunidades de adopción de animales."),
    VOLUNTEERING("Voluntariado", "Oportunidades o solicitudes relacionadas con actividades de voluntariado."),
    ANIMAL_REGISTRATION("Registro de Animal", "Confirmación o estado de un registro de animal por el usuario o censista."),
    INCIDENT("Incidente", "Reportes o actualizaciones sobre incidentes con animales (abandonos, rescates)."),
    REMINDER("Recordatorio", "Recordatorios generales sobre eventos o tareas pendientes."),
    SYSTEM("Sistema", "Notificaciones de servicio, actualizaciones de cuenta, cambios de contraseña."),
    CUSTOM_MESSAGE("Mensaje Personalizado", "Notificaciones enviadas por administradores con un mensaje específico.");

    private final String name;
    private final String description;

    NotificationTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
