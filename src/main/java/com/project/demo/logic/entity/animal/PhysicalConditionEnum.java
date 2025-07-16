package com.project.demo.logic.entity.animal;

import lombok.Getter;

/**
 * Enum representing the physical condition of an animal.
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
}
