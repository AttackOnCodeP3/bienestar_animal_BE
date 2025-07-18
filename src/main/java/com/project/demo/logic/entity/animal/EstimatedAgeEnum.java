package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enum representing the estimated age of an animal.
 * Allows friendly string conversion from JSON.
 * @author gjimenez
 */
@Getter
public enum EstimatedAgeEnum {
    PUPPY("Cachorro (menor a 6 meses)"),
    YOUNG("Joven (6 meses – 2 años)"),
    ADULT("Adulto (2 a 7 años)"),
    SENIOR("Senior (más de 7 años)");

    private final String displayName;

    EstimatedAgeEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static EstimatedAgeEnum fromDisplayName(String value) {
        return Stream.of(EstimatedAgeEnum.values())
                .filter(e -> e.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Edad estimada inválida: " + value));
    }
}
