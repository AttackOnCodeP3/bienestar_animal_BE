package com.project.demo.logic.entity.diagnostico_ia.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosticoResponseDTO {

    private String observacion;
    private String recomendaciones;
    private String advertencia;
    private String imagenUrl;
}
