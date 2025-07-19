package com.project.demo.logic.entity.sanitary_control_response;

import lombok.Getter;

/**
 * Enum of possible responses for sanitary controls.
 * Keys are in English, visible values are in Spanish.
 * @author dgutierrez
 */
@Getter
public enum SanitaryControlResponseEnum {
    YES("Sí", "Respuesta afirmativa a un control sanitario"),
    NO("No", "Respuesta negativa a un control sanitario"),
    UNKNOWN("No sé", "Respuesta desconocida o no aplicable a un control sanitario");

    private final String name;
    private final String description;


    SanitaryControlResponseEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
