package com.project.demo.logic.entity.complaint_type;

import lombok.Getter;

/**
 * Enum representing the most common types of animal welfare complaints.
 * <p>
 * - NEGLECT: Falta de provisión de necesidades básicas como alimento, agua, refugio o atención veterinaria.
 * - PHYSICAL_ABUSE: Agresión física intencional que causa daño o sufrimiento.
 * - HOARDING: Acumulación de animales en condiciones insalubres y sin cuidado adecuado.
 * - ABANDONMENT: Abandono de mascotas o animales sin supervisión.
 * - ANIMAL_FIGHTING: Peleas organizadas como perros o gallos para apuestas.
 * - TRANSPORT_WELFARE: Condiciones inadecuadas durante transporte de animales.
 * - OTHERS: Otras formas de maltrato o incumplimientos no categorizados.
 */
@Getter
public enum ComplaintTypeEnum {
    NEGLECT("Negligencia", "Falta de provisión de necesidades básicas como alimento, agua, refugio o atención veterinaria"),
    PHYSICAL_ABUSE("Abuso físico", "Daño intencional o tortura que causa sufrimiento al animal"),
    HOARDING("Acaparamiento", "Presencia de muchos animales en condiciones insalubres y sin cuidado adecuado"),
    ABANDONMENT("Abandono", "Animales dejados sin supervisión ni atención"),
    ANIMAL_FIGHTING("Peleas de animales", "Organización o participación en peleas de perros, gallos u otros"),
    TRANSPORT_WELFARE("Bienestar en transporte", "Condiciones extremas o falta de cuidado durante transporte de animales"),
    OTHERS("Otros", "Otros tipos de malos tratos o violaciones no categorizadas específicamente");

    private final String name;
    private final String description;

    ComplaintTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
