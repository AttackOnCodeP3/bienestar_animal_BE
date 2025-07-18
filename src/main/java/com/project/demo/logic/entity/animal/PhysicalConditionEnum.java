package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enum representing the physical condition of an animal.
 * The keys are in English for internal usage, but displayed in Spanish.
 * Includes JSON serialization/deserialization with display names.
 *
 * @author gjimenez
 */
@Getter
public enum PhysicalConditionEnum {
    HEALTHY("Saludable"),
    MALNOURISHED("Desnutrido"),
    WOUNDED("Herido"),
    SICK("Enfermo"),
    SKINNY("Muy delgado"),
    PARASITES("Con parásitos visibles");

    private final String displayName;

    PhysicalConditionEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static PhysicalConditionEnum fromDisplayName(String value) {
        return Stream.of(PhysicalConditionEnum.values())
                .filter(e -> e.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Condición física inválida: " + value));
    }
}
