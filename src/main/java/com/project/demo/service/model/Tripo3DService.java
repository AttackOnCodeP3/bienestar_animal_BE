package com.project.demo.service.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Tripo3DService {

    @Value("${fal.ai.api.key:}")
    private String falAiApiKey;

    @Value("${imgur.api.url}")
    private String imgurApiUrl;

    @Value("${imgur.client.id}")
    private String imgurClientId;

    @Value("${fal.api.url}")
    private String falApiUrl;

    private final WebClient webClient;

    private static final String DEFAULT_TEXTURE = "standard";
    private static final String DEFAULT_ORIENTATION = "default";
    private static final String DEFAULT_STYLE = "person:person2cartoon";

    public Tripo3DService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();

    }

    /**
     * Uploads an image to Imgur and returns the public URL.
     *
     * @param filePath Path to the image file to upload.
     * @return Public URL of the uploaded image.
     * @throws IOException If an error occurs during file reading or HTTP
     * @author nav
     */
    public String uploadToImgur(Path filePath) throws IOException {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("image", base64Image);
            formData.add("type", "base64");
            String response = webClient.post()
                    .uri(imgurApiUrl)
                    .header("Authorization", "Client-ID " + imgurClientId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Imgur response: {}", response);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            if (json.has("data") && json.get("data").has("link")) {
                return json.get("data").get("link").asText();
            } else {
                throw new RuntimeException("Could not get public URL from Imgur: " + response);
            }
        } catch (Exception e) {
            log.error("Error uploading to Imgur: " + e.getMessage());
            throw new RuntimeException("Error uploading to Imgur: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a 3D model from an image using the Tripo3D v2.5 API.
     *
     * @param imageUrl The URL of the image to convert.
     * @return The response from the Tripo3D API containing the task ID or
     * status
     * @throws IOException If an error occurs during the HTTP request or
     * response processing.
     * @throws RuntimeException If the fal.ai API key is not configured or if an
     * error occurs during the request.
     * @author nav
     */
    public String createImageToModelTaskV25(String imageUrl) throws IOException {
        try {
            if (falAiApiKey == null || falAiApiKey.trim().isEmpty()) {
                throw new RuntimeException("fal.ai API key is required for v2.5 endpoint. Please configure 'fal.ai.api.key' in application.properties");
            }
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_url", imageUrl);
            requestBody.put("texture", DEFAULT_TEXTURE);
            requestBody.put("orientation", DEFAULT_ORIENTATION);
            requestBody.put("style", DEFAULT_STYLE);
            ObjectMapper mapper = new ObjectMapper();
            log.info("=== TRIPO3D v2.5 REQUEST===");
            log.info("Request body: {}", mapper.writeValueAsString(requestBody));
            String response = webClient.post()
                    .uri(falApiUrl)
                    .header("Authorization", "Key " + falAiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Task creation response: " + response);
            return response;
        } catch (WebClientResponseException e) {
            log.error("WebClient error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            log.error("Status Code: " + e.getStatusCode());
            log.error("Response Headers: " + e.getHeaders());
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("Error 401: Invalid fal.ai API key. Please check your 'fal.ai.api.key' configuration", e);
            } else if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("Error 403: Access denied. Please check your fal.ai API key permissions", e);
            } else if (e.getStatusCode().value() == 400) {
                throw new RuntimeException("Error 400: Bad request. Check the image URL and request format: " + e.getResponseBodyAsString(), e);
            } else if (e.getStatusCode().value() == 422) {
                throw new RuntimeException("Error 422: Validation error. Check the request body format: " + e.getResponseBodyAsString(), e);
            }
            throw new RuntimeException("Error creating task with v2.5: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("General error: " + e.getMessage());
            throw new RuntimeException("Error processing v2.5 task creation: " + e.getMessage(), e);
        }
    }
}
