package com.project.demo.service.openAi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.diagnostico_ia.DiagnosticoIa;
import com.project.demo.logic.entity.diagnostico_ia.DiagnosticoIaRepository;
import com.project.demo.logic.entity.diagnostico_ia.dto.DiagnosticoRequestDTO;
import com.project.demo.logic.entity.diagnostico_ia.dto.DiagnosticoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import com.project.demo.service.model.Tripo3DService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final DiagnosticoIaRepository diagnosticoIARepository;
    private final AnimalRepository animalRepository;
    private final Tripo3DService tripo3DService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    private static final String ADVERTENCIA = "Este resultado no sustituye una consulta veterinaria.";

    public DiagnosticoResponseDTO analizarMascota(DiagnosticoRequestDTO dto) throws Exception {

        if (dto.getImagen() == null || dto.getImagen().isEmpty()) {
            throw new IllegalArgumentException("La imagen es obligatoria.");
        }
        if (dto.getImagen().getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen excede el tamaño máximo de 10MB.");
        }
        String tipo = dto.getImagen().getContentType();
        if (tipo == null || !(tipo.equalsIgnoreCase("image/jpeg") || tipo.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG o PNG.");
        }
        if (dto.getDescripcion() != null && dto.getDescripcion().length() > 250) {
            throw new IllegalArgumentException("La descripción no puede exceder 250 caracteres.");
        }
        if (dto.getAnimalId() == null) {
            throw new IllegalArgumentException("El ID del animal es obligatorio.");
        }

        Path tempFile = Files.createTempFile("observacion-" + System.nanoTime(), dto.getImagen().getOriginalFilename());
        String imagenUrl = null;
        String base64Imagen = Base64Utils.encodeToString(dto.getImagen().getBytes());
        try {
            dto.getImagen().transferTo(tempFile.toFile());
            imagenUrl = tripo3DService.uploadToImgur(tempFile);

            String prompt = "Eres un veterinario experto. Observa la imagen y describe signos visuales que puedan sugerir problemas de salud, como desnutrición, enfermedades de la piel, infecciones o lesiones. No hagas un diagnóstico definitivo, solo menciona posibles condiciones que podrían explicar su apariencia, y proporciona una lista de recomendaciones generales para mejorar su bienestar. Usa lenguaje neutral, sin afirmaciones médicas definitivas.";
            if (dto.getDescripcion() != null && !dto.getDescripcion().isBlank()) {
                prompt += "Descripción del usuario: " + dto.getDescripcion() + ". ";
            }
            prompt += "Responde en español, separa claramente el diagnóstico y las recomendaciones. Termina SIEMPRE con el mensaje: \"" + ADVERTENCIA + "\"";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", "gpt-4o");
            payload.put("max_tokens", 500);

            ArrayNode messages = mapper.createArrayNode();

            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", "Eres un asistente veterinario virtual.");
            messages.add(systemMsg);

            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");

            ArrayNode contentArr = mapper.createArrayNode();
            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("type", "text");
            textNode.put("text", prompt);
            contentArr.add(textNode);

            ObjectNode imageNode = mapper.createObjectNode();
            imageNode.put("type", "image_url");
            ObjectNode imageUrlNode = mapper.createObjectNode();
            imageUrlNode.put("url", "data:image/jpeg;base64," + base64Imagen);
            imageNode.set("image_url", imageUrlNode);
            contentArr.add(imageNode);

            userMsg.set("content", contentArr);
            messages.add(userMsg);

            payload.set("messages", messages);

            String jsonPayload = mapper.writeValueAsString(payload);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(openAiApiUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al comunicarse con OpenAI.");
            }

            JsonNode root = mapper.readTree(response.getBody());
            String contenido = root.path("choices").get(0).path("message").path("content").asText();

            String observacion = contenido;
            String recomendaciones = "";
            if (contenido.contains("Recomendaciones:")) {
                String[] partes = contenido.split("Recomendaciones:", 2);
                observacion = partes[0].trim();
                recomendaciones = partes[1].replace(ADVERTENCIA, "").trim();
            }
            if (!contenido.contains(ADVERTENCIA)) {
                recomendaciones = recomendaciones + "\n" + ADVERTENCIA;
            }

            Optional<Animal> animalOpt = animalRepository.findById(dto.getAnimalId());
            if (animalOpt.isEmpty()) {
                throw new IllegalArgumentException("Animal no encontrado.");
            }
            DiagnosticoIa diagnosticoIa = DiagnosticoIa.builder()
                    .animal(animalOpt.get())
                    .descripcionUsuario(dto.getDescripcion())
                    .diagnosticoIA(contenido)
                    .imagenUrl(imagenUrl)
                    .build();
            diagnosticoIARepository.save(diagnosticoIa);

            return DiagnosticoResponseDTO.builder()
                    .observacion(observacion)
                    .recomendaciones(recomendaciones)
                    .advertencia(ADVERTENCIA)
                    .imagenUrl(imagenUrl)
                    .build();
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                log.warn("No se pudo eliminar el archivo temporal: {}", tempFile);
            }
        }
    }
}
