package com.project.demo.rest.complaint.dto;

import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.rest.complaint_state.dto.ComplaintStateDTO;
import com.project.demo.rest.complaint_type.dto.ComplaintTypeDTO;

/**
 * Data Transfer Object (DTO) for Complaint.
 * <p>
 * This DTO is used to encapsulate the necessary fields for representing a complaint,
 * including its ID, description, image URL, coordinates, complaint type name, and complaint state name.
 * It provides a static method to convert from a Complaint entity to this DTO.
 *
 * @param id                 The unique identifier of the complaint.
 * @param description        The description of the complaint.
 * @param imageUrl           The URL of the image associated with the complaint.
 * @param latitude           The latitude coordinate of the complaint location.
 * @param longitude          The longitude coordinate of the complaint location.
 * @param complaintTypeDTO  The type of the complaint, represented as a ComplaintTypeDTO.
 * @param observations        Additional observations related to the complaint.
 * @param complaintStateDTO  The state of the complaint, represented as a ComplaintStateDTO.
 * <p>
 * @author dgutierrez
 */
public record ComplaintDTO(
        Long id,
        String description,
        String imageUrl,
        Double latitude,
        Double longitude,
        String observations,
        ComplaintTypeDTO complaintTypeDTO,
        ComplaintStateDTO complaintStateDTO
) {
    public static ComplaintDTO fromEntity(Complaint complaint) {
        return new ComplaintDTO(
                complaint.getId(),
                complaint.getDescription(),
                complaint.getImageUrl(),
                complaint.getLatitude(),
                complaint.getLongitude(),
                complaint.getObservations(),
                ComplaintTypeDTO.fromEntity(complaint.getComplaintType()),
                ComplaintStateDTO.fromEntity(complaint.getComplaintState())
        );
    }
}

