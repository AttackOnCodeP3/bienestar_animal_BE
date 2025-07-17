package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enum representing the physical condition of an animal.
 * Allows friendly string conversion from JSON.
 * @author gjimenez
 */
@Getter
public enum PhysicalConditionEnum {
    SALUDABLE("Saludable"),
    DESNUTRIDO("Desnutrido"),
    HERIDO("Herido"),
    ENFERMO("Enfermo"),
    MUY_DELGADO("Muy delgado"),
    CON_PARASITOS_VISIBLES("Con parásitos visibles");

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
                .orElseThrow(() -> new IllegalArgumentException("Invalid physical condition: " + value));
    }
}
