package com.project.demo.rest.model.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadPictureResponseDTO {
    private boolean success;
    private String message;
    private String publicImageUrl;
}