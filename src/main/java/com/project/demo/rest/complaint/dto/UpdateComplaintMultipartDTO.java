package com.project.demo.rest.complaint.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for updating a complaint with multipart file upload.
 * This DTO is used to encapsulate the data required for updating a complaint,
 * including the description, geographical coordinates, complaint type ID, and an image file.
 *
 * @param description   the description of the complaint
 * @param latitude      the latitude of the complaint location
 * @param longitude     the longitude of the complaint location
 * @param complaintTypeId the ID of the complaint type
 * @param image         the image file associated with the complaint
 * @author dgutierrez
 */
public record UpdateComplaintMultipartDTO(
        String description,
        Double latitude,
        Double longitude,
        Long complaintTypeId,
        MultipartFile image
) {
}
