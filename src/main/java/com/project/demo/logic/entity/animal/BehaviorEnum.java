package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enum representing the behavior or posture of an animal.
 * Allows friendly string conversion from JSON.
 * @author gjimenez
 */
@Getter
public enum BehaviorEnum {
    ACOSTADO("Acostado"),
    DE_PIE("De pie"),
    SENTADO("Sentado"),
    CAMINANDO("Caminando"),
    ASUSTADO("Asustado"),
    AGRESIVO("Agresivo"),
    DESCANSANDO("Descansando");

    private final String displayName;

    BehaviorEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BehaviorEnum fromDisplayName(String value) {
        return Stream.of(BehaviorEnum.values())
                .filter(e -> e.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid behavior: " + value));
    }
}
