package com.project.demo.logic.entity.municipality;

import lombok.Getter;

/**
 * Enum that represents the status of a municipality.
 * @author gjimenez
 * {@code @modifiedBy} dgutierrez 11/07/2025 add the JavaDoc and the description of the enum values.
 */
@Getter
public enum MunicipalityStatusEnum {
    ACTIVE("Activo", "Municipio activo y operativo."),
    DEACTIVATED("Desactivado", "Municipio desactivado temporalmente."),
    ARCHIVED("Archivado", "Municipio archivado y sin operaciones activas.");

    private final String displayName;
    private final String description;

    MunicipalityStatusEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}