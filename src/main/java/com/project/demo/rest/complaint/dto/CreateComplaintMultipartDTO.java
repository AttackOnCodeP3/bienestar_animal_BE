package com.project.demo.rest.complaint.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for creating a complaint with image upload.
 * <p>
 * This DTO is used to encapsulate the necessary fields for registering a complaint,
 * including its description, coordinates, complaint type, and an optional image file.
 * The user (createdBy) will be inferred from the token, so it's not included here.
 *
 * @author dgutierrez
 */
@Getter
@Setter
public class CreateComplaintMultipartDTO {
    private String description;
    private Double latitude;
    private Double longitude;
    private Long complaintTypeId;
    private MultipartFile image;
}
