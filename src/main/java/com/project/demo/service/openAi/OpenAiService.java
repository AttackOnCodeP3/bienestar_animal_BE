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
import com.project.demo.service.model.Tripo3DService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
    private static final String ADVERTENCIA = "Recuerda que esta evaluación es solo orientativa y no sustituye la consulta veterinaria presencial.";

    private final AnimalRepository animalRepository;
    private final DiagnosticoIaRepository diagnosticoIaRepository;
    private final Tripo3DService tripo3DService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    public OpenAiService(
            AnimalRepository animalRepository,
            DiagnosticoIaRepository diagnosticoIaRepository,
            Tripo3DService tripo3DService
    ) {
        this.animalRepository = animalRepository;
        this.diagnosticoIaRepository = diagnosticoIaRepository;
        this.tripo3DService = tripo3DService;
    }

    /* Método para analizar la mascota 
     * Recibe un DTO con la imagen y descripción del animal,
     * valida los datos, envía la imagen a OpenAI y procesa la respuesta.
     * @throws Exception si ocurre un error durante el proceso.
     * @return DiagnosticoResponseDTO con la observación, recomendaciones y advertencia.
     * @param dto DiagnosticoRequestDTO que contiene la imagen y descripción del animal.
     * @author nav
     * 
    */
    public DiagnosticoResponseDTO analizarMascota(DiagnosticoRequestDTO dto) throws Exception {
        validateDiagnosticoRequest(dto);

        Path tempFile = null;
        String imagenUrl = null;
        String base64Imagen = Base64.getEncoder().encodeToString(dto.getImagen().getBytes());

        try {
            tempFile = Files.createTempFile("observacion-" + System.nanoTime(), dto.getImagen().getOriginalFilename());
            dto.getImagen().transferTo(tempFile.toFile());
            imagenUrl = tripo3DService.uploadToImgur(tempFile);

            String prompt = buildPrompt(dto.getDescripcion());
            String jsonPayload = buildOpenAiPayload(prompt, base64Imagen);
            ResponseEntity<String> response = sendOpenAiRequest(jsonPayload);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al comunicarse con OpenAI.");
            }

            return processOpenAiResponse(response.getBody(), dto, imagenUrl);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    log.warn("No se pudo eliminar el archivo temporal: {}", tempFile);
                }
            }
        }
    }

    /* Método para validar la solicitud de diagnóstico.
     * Verifica que la imagen no sea nula, que su tamaño no exceda 10MB,
     * que el tipo de imagen sea JPG o PNG, y que la descripción y el ID del animal sean válidos.
     * @throws IllegalArgumentException si alguna validación falla.
     * @param dto DiagnosticoRequestDTO con los datos a validar.
     * @author nav
     */
    private void validateDiagnosticoRequest(DiagnosticoRequestDTO dto) {
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
    }

    /* Método para construir el prompt que se enviará a OpenAI.
     * Incluye una descripción del usuario si está presente y termina con una advertencia.
     * @param descripcion Descripción proporcionada por el usuario.
     * @return String con el prompt completo.
     * @author nav
     */
    private String buildPrompt(String descripcion) {
        String prompt = "Eres un veterinario experto. Observa la imagen y describe signos visuales que puedan sugerir problemas de salud, como desnutrición, enfermedades de la piel, infecciones o lesiones. No hagas un diagnóstico definitivo, solo menciona posibles condiciones que podrían explicar su apariencia, y proporciona una lista de recomendaciones generales para mejorar su bienestar. Usa lenguaje neutral, sin afirmaciones médicas definitivas.";
        if (descripcion != null && !descripcion.isBlank()) {
            prompt += " Descripción del usuario: " + descripcion + ".";
        }
        prompt += " Responde en español, separa claramente el diagnóstico y las recomendaciones. Termina SIEMPRE con el mensaje: \"" + ADVERTENCIA + "\"";
        return prompt;
    }

    /* Método para construir el payload JSON que se enviará a OpenAI.
     * Incluye el modelo, mensajes del sistema y del usuario con la imagen en base64.
     * @param prompt Texto del prompt a enviar.
     * @param base64Imagen Imagen codificada en base64.
     * @return String con el payload JSON completo.
     * @throws Exception si ocurre un error al construir el JSON.
     * @author nav
     */
    private String buildOpenAiPayload(String prompt, String base64Imagen) throws Exception {
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

        return mapper.writeValueAsString(payload);
    }

    /* Método para enviar la solicitud a OpenAI.
     * Utiliza RestTemplate para hacer una petición POST con el payload JSON.
     * @param jsonPayload Payload JSON a enviar.
     * @return ResponseEntity con la respuesta de OpenAI.
     * @throws Exception si ocurre un error durante la solicitud.
     * @author nav
     */
    private ResponseEntity<String> sendOpenAiRequest(String jsonPayload) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        return restTemplate.exchange(openAiApiUrl, HttpMethod.POST, entity, String.class);
    }
    /* Método para procesar la respuesta de OpenAI.
     * Extrae el contenido del diagnóstico y las recomendaciones, y guarda el diagnóstico en la base de datos.
     * @param responseBody Cuerpo de la respuesta de OpenAI.
     * @param dto DiagnosticoRequestDTO con los datos del diagnóstico.
     * @param imagenUrl URL de la imagen procesada.
     * @return DiagnosticoResponseDTO con la observación, recomendaciones y advertencia.
     * @throws Exception si ocurre un error al procesar la respuesta.
     * @author nav
     */

    private DiagnosticoResponseDTO processOpenAiResponse(String responseBody, DiagnosticoRequestDTO dto, String imagenUrl) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
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
        diagnosticoIaRepository.save(diagnosticoIa);

        return DiagnosticoResponseDTO.builder()
                .observacion(observacion)
                .recomendaciones(recomendaciones)
                .advertencia(ADVERTENCIA)
                .imagenUrl(imagenUrl)
                .build();
    }
}