package com.project.demo.logic.entity.vaccine;

import lombok.Getter;

/**
 * Enum representing different types of vaccines.
 * Each vaccine has a name and a description.
 * @author dgutierrez
 */
@Getter
public enum VaccineEnum {
    RABIES("Rabia", "Vacuna contra el virus de la rabia"),
    MULTIPLE("Múltiple", "Vacuna múltiple contra diversas enfermedades"),
    QUINTUPLE("Quíntuple", "Vacuna que protege contra 5 enfermedades comunes");

    private final String name;
    private final String description;

    VaccineEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

