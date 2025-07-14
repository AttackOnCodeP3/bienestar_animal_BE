package com.project.demo.rest.model.dto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateTaskResponseDTO {
        private boolean success;
    private String message;
    private String taskId;
    private String imageUrl;
    private JsonNode modelMesh;
    private JsonNode renderedImage;
    
}
