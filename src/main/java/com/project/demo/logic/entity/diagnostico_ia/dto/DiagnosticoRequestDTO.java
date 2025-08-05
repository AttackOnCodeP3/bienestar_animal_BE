package com.project.demo.logic.entity.diagnostico_ia.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class DiagnosticoRequestDTO {

    private Long animalId;
    private MultipartFile imagen;
    private String descripcion;
}
