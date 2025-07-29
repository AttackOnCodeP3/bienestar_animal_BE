package com.project.demo.rest.announcement.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAnnouncementMultipartDTO {
    private String title;
    private String description;
    private Long stateId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile file;
}
