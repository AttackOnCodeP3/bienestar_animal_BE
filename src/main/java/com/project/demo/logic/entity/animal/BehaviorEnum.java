package com.project.demo.logic.entity.animal;

import lombok.Getter;

/**
 * Enum representing the behavior or posture of an animal.
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
}
