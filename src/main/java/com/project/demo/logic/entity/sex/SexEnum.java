package com.project.demo.logic.entity.sex;

import lombok.Getter;

/**
 * Enum representing different sexes
 * @author dgutierrez
 */
@Getter
public enum SexEnum {

    MALE("Masculino"),
    FEMALE("Femenino");

    private final String name;

    SexEnum(String name) {
        this.name = name;
    }
}
