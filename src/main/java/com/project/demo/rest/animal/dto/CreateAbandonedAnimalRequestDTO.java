package com.project.demo.rest.animal.dto;

import com.project.demo.logic.entity.animal.EstimatedAgeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for creating an abandoned animal record.
 * @author gjimenez
 */
@Getter
@Setter
public class CreateAbandonedAnimalRequestDTO {
    private String species;
    private String sex;
    private EstimatedAgeEnum estimatedAge;
    private String physicalCondition;
    private String behavior;
    private String district;
    private String neighborhood;
    private String observations;
    private Double latitude;
    private Double longitude;
    private String photoBase64;
}
