package com.project.demo.logic.entity.complaint_state;

import lombok.Getter;

/**
 * Enum representing the different states a complaint can go through.
 * <p>
 * - OPEN: Denuncia recién creada y pendiente de revisión.
 * - APPROVED: Denuncia ha sido aprobada para su seguimiento o atención.
 * - WITH_OBSERVATIONS: Denuncia presenta observaciones que deben resolverse.
 * - COMPLETED: Denuncia ha sido atendida y se considera finalizada.
 */
@Getter
public enum ComplaintStateEnum {
    OPEN("Abierta", "Denuncia recién creada y pendiente de revisión."),
    APPROVED("Aprobada", "Denuncia validada y aceptada para su procesamiento."),
    WITH_OBSERVATIONS("Con observaciones", "La denuncia contiene observaciones que deben corregirse."),
    COMPLETED("Completada", "Denuncia atendida satisfactoriamente y cerrada."),
    CLOSED("Cerrada", "Denuncia no aprobada y cerrada por el usuario."),
    CANCELLED("Cancelada", "Denuncia cancelada por el usuario antes de su procesamiento.");

    private final String name;
    private final String description;

    ComplaintStateEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
