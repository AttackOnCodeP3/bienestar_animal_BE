package com.project.demo.rest.diagnostico;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.diagnostico_ia.DiagnosticoIa;
import com.project.demo.logic.entity.diagnostico_ia.DiagnosticoIaRepository;
import com.project.demo.logic.entity.diagnostico_ia.dto.DiagnosticoRequestDTO;
import com.project.demo.logic.entity.diagnostico_ia.dto.DiagnosticoResponseDTO;
import com.project.demo.service.openAi.OpenAiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/diagnostico")
@RequiredArgsConstructor
public class DiagnosticoController {
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoController.class);
    private final OpenAiService openAiService;
    private final DiagnosticoIaRepository diagnosticoIARepository;
    private final AnimalRepository animalRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analizar(
            @RequestParam("animalId") Long animalId,
            @RequestPart("imagen") MultipartFile imagen,
            @RequestPart(value = "descripcion", required = false) String descripcion,
            HttpServletRequest request
    ) {
        try {
            DiagnosticoRequestDTO dto = new DiagnosticoRequestDTO();
            dto.setAnimalId(animalId);
            dto.setImagen(imagen);
            dto.setDescripcion(descripcion);

            DiagnosticoResponseDTO response = openAiService.analizarMascota(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en diagnóstico IA", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

   @GetMapping
public ResponseEntity<?> getDiagnosticosByAnimalId(
        @RequestParam("animalId") Long animalId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Optional<Animal> animalOpt = animalRepository.findById(animalId);
    if (animalOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
    }

    Pageable pageable = PaginationUtils.buildPageable(page, size);
Page<DiagnosticoIa> diagnosticosPage = diagnosticoIARepository.findByAnimalId(animalId, pageable);

    List<Map<String, Object>> items = diagnosticosPage.getContent().stream().map(d -> {
        Map<String, Object> obj = new HashMap<>();
        obj.put("id", d.getId());
        obj.put("imagenUrl", d.getImagenUrl());
        obj.put("description", d.getDescripcionUsuario());
        obj.put("createdAt", d.getCreatedAt());
        obj.put("observacion", d.getDiagnosticoIA());
        obj.put("advertencia", "");
        return obj;
    }).toList();

    Map<String, Object> response = new HashMap<>();
    response.put("items", items);
    response.put("totalItems", diagnosticosPage.getTotalElements());
    response.put("totalPages", diagnosticosPage.getTotalPages());
    response.put("currentPage", page);

    return ResponseEntity.ok(response);
}

}
