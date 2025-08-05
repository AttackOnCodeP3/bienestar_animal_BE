package com.project.demo.rest.complaint.dto;

import com.project.demo.logic.entity.complaint.Complaint;

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
 * @param complaintTypeName  The name of the type of the complaint.
 * @param complaintStateName The name of the state of the complaint.
 * @author dgutierrez
 */
public record ComplaintDTO(
        Long id,
        String description,
        String imageUrl,
        Double latitude,
        Double longitude,
        String observations,
        String complaintTypeName,
        String complaintStateName
) {
    public static ComplaintDTO fromEntity(Complaint complaint) {
        return new ComplaintDTO(
                complaint.getId(),
                complaint.getDescription(),
                complaint.getImageUrl(),
                complaint.getLatitude(),
                complaint.getLongitude(),
                complaint.getObservations(),
                complaint.getComplaintType().getName(),
                complaint.getComplaintState().getName()
        );
    }
}

