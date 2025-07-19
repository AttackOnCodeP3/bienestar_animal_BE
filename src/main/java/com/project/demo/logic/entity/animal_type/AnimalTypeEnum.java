package com.project.demo.logic.entity.animal_type;

import lombok.Getter;

@Getter
public enum AnimalTypeEnum {
    COMMUNITY_ANIMAL("Animal de comunidad", "Animal que vive en una comunidad, generalmente con dueño específico"),
    ABANDONED_ANIMAL("Animal abandonado", "Animal que vive en una comunidad, generalmente sin dueño específico");

    private final String name;
    private final String description;

    AnimalTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
