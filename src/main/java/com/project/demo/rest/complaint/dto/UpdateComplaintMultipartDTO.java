package com.project.demo.rest.complaint.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateComplaintMultipartDTO(
        String description,
        Double latitude,
        Double longitude,
        Long complaintTypeId,
        MultipartFile image
) {
}
