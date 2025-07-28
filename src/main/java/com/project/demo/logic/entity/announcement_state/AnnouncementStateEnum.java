package com.project.demo.logic.entity.announcement_state;

import lombok.Getter;

/**
 * Enum representing the different states of an announcement.
 * Possible values are: Draft, Published, and Archived.
 * <p>
 * - DRAFT: The announcement is being edited and is not yet visible to users.
 * - PUBLISHED: The announcement is visible to users.
 * - ARCHIVED: The announcement is no longer active but kept for record.
 *
 * @author dgutierrez
 */
@Getter
public enum AnnouncementStateEnum {
    DRAFT("En borrador", "El anuncio está siendo editado y aún no es visible para los usuarios"),
    PUBLISHED("Publicado", "El anuncio está visible para los usuarios"),
    ARCHIVED("Archivado", "El anuncio ya no está activo, pero se mantiene para registro");

    private final String name;
    private final String description;

    AnnouncementStateEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
