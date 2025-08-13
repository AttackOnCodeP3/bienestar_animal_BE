package com.project.demo.rest.complaint_state.dto;

import com.project.demo.logic.entity.complaint_state.ComplaintState;

/**
 * Data Transfer Object (DTO) for ComplaintState.
 * <p>
 * This DTO is used to encapsulate the necessary fields for a complaint state,
 * including its ID, name, and description.
 *
 * @param id          The unique identifier of the complaint state.
 * @param name        The name of the complaint state.
 * @author dgutierrez
 */
public record ComplaintStateDTO(
        Long id,
        String name
) {
    public static ComplaintStateDTO fromEntity(ComplaintState entity) {
        return new ComplaintStateDTO(
                entity.getId(),
                entity.getName()
        );
    }
}
