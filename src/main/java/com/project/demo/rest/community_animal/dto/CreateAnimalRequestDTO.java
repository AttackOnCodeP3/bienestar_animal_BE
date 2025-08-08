package com.project.demo.rest.community_animal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new animal in the community.
 * Contains fields for the animal's name, birthdate, weight, species
 * @author dgutierrez
 * @modifiedBy gjimenez - Added support to register animals for a different user.
 */
@Getter
@Setter
public class CreateAnimalRequestDTO {
    private String name;
    private LocalDate birthDate;
    private double weight;
    private Long speciesId;
    private Long sexId;
    private Long raceId;
    private Double latitude;
    private Double longitude;
    private List<SanitaryControlDTO> sanitaryControls;
    private List<VaccineApplicationDTO> vaccineApplications;
    private String ownerIdentificationCard;
}
