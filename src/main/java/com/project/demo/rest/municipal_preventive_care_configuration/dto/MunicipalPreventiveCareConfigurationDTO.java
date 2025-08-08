package com.project.demo.rest.municipal_preventive_care_configuration.dto;

import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfiguration;

/**
 * Data Transfer Object (DTO) for MunicipalPreventiveCareConfiguration.
 * @param id The unique identifier of the configuration.
 * @param type The type of preventive care configuration, represented as a string.
 * @param value The value associated with the configuration, typically representing a frequency in months.
 * @author dgutierrez
 */
public record MunicipalPreventiveCareConfigurationDTO(
        Long id,
        String type,
        int value
) {
    public static MunicipalPreventiveCareConfigurationDTO fromEntity(MunicipalPreventiveCareConfiguration entity) {
        return new MunicipalPreventiveCareConfigurationDTO(
                entity.getId(),
                entity.getType().getName(),
                entity.getValue()
        );
    }
}
