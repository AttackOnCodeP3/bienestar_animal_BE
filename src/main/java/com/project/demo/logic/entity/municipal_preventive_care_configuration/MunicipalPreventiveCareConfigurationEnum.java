package com.project.demo.logic.entity.municipal_preventive_care_configuration;

import lombok.Getter;

@Getter
public enum MunicipalPreventiveCareConfigurationEnum {
    VACCINATION("Vacunación", "Frecuencia en meses para vacunación"),
    DEWORMING("Desparasitación", "Frecuencia en meses para desparasitación"),
    FLEA_TREATMENT("Pulguicida", "Frecuencia en meses para tratamiento contra pulgas");

    private final String name;
    private final String description;

    MunicipalPreventiveCareConfigurationEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

