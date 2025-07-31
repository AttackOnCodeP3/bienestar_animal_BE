package com.project.demo.rest.announcement.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * DTO for updating announcements via multipart/form-data.
 * Allows optional update of image file.
 * Used in PUT /announcements/my-municipality/{id}
 * @author dgutierrez
 */
@Getter
@Setter
public class UpdateAnnouncementMultipartDTO {
    private String title;
    private String description;
    private Long stateId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile file;
}