package com.project.demo.logic.entity.animal;

import lombok.Getter;

/**
 * Enum representing the estimated age of an animal.
 *  @author gjimenez
 */
@Getter
public enum EstimatedAgeEnum {
    CACHORRO("Cachorro (menor a 6 meses)"),
    JOVEN("Joven (6 meses – 2 años)"),
    ADULTO("Adulto (2 a 7 años)"),
    SENIOR("Senior (más de 7 años)");

    private final String displayName;

    EstimatedAgeEnum(String displayName) {
        this.displayName = displayName;
    }
}
