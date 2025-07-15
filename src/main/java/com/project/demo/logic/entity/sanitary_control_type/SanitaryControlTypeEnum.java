package com.project.demo.logic.entity.sanitary_control_type;

import lombok.Getter;

/**
 * Enum representing different types of sanitary controls.
 * @author dgutierrez
 */
@Getter
public enum SanitaryControlTypeEnum {
    VACCINATION("Vacunación", "Vacunación contra enfermedades"),
    DEWORMING("Desparasitación", "Tratamiento para eliminar parásitos intestinales"),
    FLEA_AND_TICK_CONTROL("Control de pulgas y garrapatas", "Tratamiento para controlar pulgas y garrapatas"),
    NEUTERING("Esterilización", "Procedimiento quirúrgico para esterilizar animales");

    private final String name;
    private final String description;

    SanitaryControlTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
