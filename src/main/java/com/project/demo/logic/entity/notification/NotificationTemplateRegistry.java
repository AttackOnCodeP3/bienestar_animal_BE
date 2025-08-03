package com.project.demo.logic.entity.notification;

import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry that maps each NotificationTypeEnum to its default template.
 * Used to generate notification content programmatically based on type.
 *
 * @author dgutierrez
 */
public class NotificationTemplateRegistry {

    private static final Map<NotificationTypeEnum, NotificationTemplate> TEMPLATE_MAP = new EnumMap<>(NotificationTypeEnum.class);

    static {
        TEMPLATE_MAP.put(NotificationTypeEnum.CAMPAIGN, new NotificationTemplate("Nueva campaña municipal", "Se ha publicado una campaña sobre vacunación o adopción.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.SANITARY_ALERT, new NotificationTemplate("Alerta sanitaria", "Recuerde realizar el control sanitario de su animal.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.ADOPTION, new NotificationTemplate("Oportunidad de adopción", "Hay nuevas mascotas disponibles para adoptar.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.VOLUNTEERING, new NotificationTemplate("Voluntariado disponible", "Se ha abierto una oportunidad de voluntariado.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.ANIMAL_REGISTRATION, new NotificationTemplate("Registro actualizado", "El estado del registro de su animal ha cambiado.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.INCIDENT, new NotificationTemplate("Incidente reportado", "Se ha registrado un incidente con un animal en su zona.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.REMINDER, new NotificationTemplate("Recordatorio", "Tiene actividades o eventos pendientes.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.SYSTEM, new NotificationTemplate("Notificación del sistema", "Actualización importante de su cuenta.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.ANNOUNCEMENT_CREATED, new NotificationTemplate("Nuevo anuncio", "Se ha publicado un nuevo anuncio en su municipalidad.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.CUSTOM_MESSAGE, new NotificationTemplate("Mensaje del administrador", "Tiene un mensaje personalizado en su bandeja.", ""));

        TEMPLATE_MAP.put(NotificationTypeEnum.COMPLAINT, new NotificationTemplate("Denuncia recibida", "Se ha registrado una nueva denuncia.", ""));
    }

    public static NotificationTemplate getTemplate(NotificationTypeEnum type) {
        return TEMPLATE_MAP.get(type);
    }
}
