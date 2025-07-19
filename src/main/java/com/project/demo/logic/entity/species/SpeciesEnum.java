package com.project.demo.logic.entity.species;

import lombok.Getter;

/**
 * Enum representing different species of animals.
 * Each species has a name and a description.
 * @author dgutierrez
 */
@Getter
public enum SpeciesEnum {
    DOG("Perro", "Animal doméstico conocido por su lealtad y compañía"),
    CAT("Gato", "Animal doméstico conocido por su independencia y agilidad");

    private final String name;
    private final String description;

    SpeciesEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
