package com.project.demo.rest.announcement.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
/**
 * Data Transfer Object (DTO) for creating announcements with multipart file uploads.
 * <p>
 * This class is used to encapsulate the data required for creating an announcement,
 * including the title, description, state ID, start and end dates, and an optional
 * image file. The {@link MultipartFile} field allows for uploading an image
 * associated with the announcement.
 */
public class CreateAnnouncementMultipartDTO {
    private String title;
    private String description;
    private Long stateId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile file;
}
