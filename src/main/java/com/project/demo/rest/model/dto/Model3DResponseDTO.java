package com.project.demo.rest.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class Model3DResponseDTO {
    private Long id;
    private String photoOriginalUrl;
    private String urlModelo;
    private Long animalId;
    private String animalName;
    private String stateGenerationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}