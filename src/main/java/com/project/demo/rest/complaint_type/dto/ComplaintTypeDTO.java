package com.project.demo.rest.complaint_type.dto;

import com.project.demo.logic.entity.complaint_type.ComplaintType;

/**
 * Data Transfer Object (DTO) for ComplaintType.
 * <p>
 * This DTO is used to encapsulate the necessary fields for representing a complaint type,
 * including its ID and name. It provides a static method to convert from a ComplaintType entity to this DTO.
 *
 * @param id   The unique identifier of the complaint type.
 * @param name The name of the complaint type.
 * @author dgutierrez
 */
public record ComplaintTypeDTO(
        Long id,
        String name,
        String description
) {
    public static ComplaintTypeDTO fromEntity(ComplaintType complaintType) {
        return new ComplaintTypeDTO(
                complaintType.getId(),
                complaintType.getName(),
                complaintType.getDescription()
        );
    }
}
