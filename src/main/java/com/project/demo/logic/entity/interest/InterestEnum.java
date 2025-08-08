package com.project.demo.logic.entity.interest;

import lombok.Getter;

/**
 * Enum representing predefined types of user interests related to animal welfare activities.
 * Each interest has a name and description for seeding into the database.
 *
 * @author dgutierrez
 */
@Getter
public enum InterestEnum {
    ADOPT("Adoptar", "Interés en adoptar un animal"),
    TEMPORARY_HOME("Apoyar como casa cuna (cuido de animales temporalmente)", "Interés en brindar hogar temporal a animales"),
    MUNICIPAL_EVENTS("Apoyar en actividades de la Municipalidad, ejemplo: castración, festivales, capacitador(a)", "Interés en participar en actividades organizadas por la municipalidad"),
    DONATE_FOOD("Donar alimentos", "Interés en donar alimentos para animales"),
    RECEIVE_TRAINING("Recibir capacitación", "Interés en recibir capacitación relacionada con el bienestar animal");

    private final String name;
    private final String description;

    InterestEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}