package com.project.demo.service.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.model3D.Model3D;
import com.project.demo.logic.entity.model3D.Model3DRepository;
import com.project.demo.logic.entity.state.StateGeneration;
import com.project.demo.logic.entity.state.StateGenerationRepository;
import com.project.demo.rest.model.dto.CreateTaskResponseDTO;
import com.project.demo.rest.model.dto.Model3DResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing 3D model entities.
 * Handles creation and updates of Model3D entities based on task responses.
 * 
 * @author nav
 */
@Slf4j
@Service
public class Model3DService {
    
    private final Model3DRepository model3DRepository;
    private final StateGenerationRepository stateGenerationRepository;
    
    public Model3DService(Model3DRepository model3DRepository, 
                         StateGenerationRepository stateGenerationRepository) {
        this.model3DRepository = model3DRepository;
        this.stateGenerationRepository = stateGenerationRepository;
    }

        /**
     * Finds a 3D model by animal ID.
     * @param animalId The ID of the animal
     * @return Optional containing the Model3D if found
     * @author nav
     */
 public Optional<Model3DResponseDTO> findByAnimalId(Long animalId) {
    log.info("Searching for Model3D with animal ID: {}", animalId);
    try {
        Optional<Model3D> result = model3DRepository.findByAnimalId(animalId);
        if (result.isPresent()) {
            log.info("Found Model3D for animal ID: {}", animalId);
        } else {
            log.info("No Model3D found for animal ID: {}", animalId);
        }
        return result.map(this::convertToDTO);
    } catch (Exception e) {
        log.error("Error searching for Model3D with animal ID: {} - Error: {}", animalId, e.getMessage(), e);
        throw e;
    }
}

    /**
     * Converts Model3D entity to DTO.
     * @param model3D The Model3D entity
     * @return Model3DResponseDTO
     * @author nav
     */
    private Model3DResponseDTO convertToDTO(Model3D model3D) {
        return Model3DResponseDTO.builder()
            .id(model3D.getId())
            .photoOriginalUrl(model3D.getPhotoOriginalUrl())
            .urlModelo(model3D.getUrlModelo())
            .animalId(model3D.getAnimal() != null ? model3D.getAnimal().getId() : null)
            .animalName(model3D.getAnimal() != null ? model3D.getAnimal().getName() : null)
            .stateGenerationName(model3D.getStateGeneration() != null ? model3D.getStateGeneration().getName() : null)
            .createdAt(model3D.getCreatedAt())
            .updatedAt(model3D.getUpdatedAt())
            .build();
    }
    
    /**
     * Saves or updates a 3D model based on the task response.
     * @param taskResponse Response from the 3D model generation task
     * @param animal The animal associated with the model
     * @return The saved Model3D entity
     * @throws RuntimeException if the state is not found
     * This method will create a new Model3D if it doesn't exist,
     * or update the existing one if it does.
     * It sets the state based on the success of the task response,
     * and extracts URLs from the JsonNode provided in the response.        
     * @author nav
     */
    @Transactional
    public Model3D saveModel3DFromTaskResponse(CreateTaskResponseDTO taskResponse, Animal animal) {
        String stateName = taskResponse.isSuccess() ? "Generado" : "Error";
        StateGeneration state = stateGenerationRepository.findByName(stateName)
            .orElseThrow(() -> new RuntimeException("State not found: " + stateName));
        
        String photoOriginalUrl = extractUrlFromJsonNode(taskResponse.getRenderedImage());
        String urlModelo = extractUrlFromJsonNode(taskResponse.getModelMesh());
        
        Optional<Model3D> existingModel = model3DRepository.findByAnimalId(animal.getId());
        
        Model3D model3D;
        if (existingModel.isPresent()) {
            model3D = existingModel.get();
            model3D.setPhotoOriginalUrl(photoOriginalUrl);
            model3D.setUrlModelo(urlModelo);
            model3D.setStateGeneration(state);
            log.info("Updated existing Model3D with ID: {} for animal: {}", model3D.getId(), animal.getId());
        } else {
            model3D = Model3D.builder()
                .photoOriginalUrl(photoOriginalUrl)
                .urlModelo(urlModelo)
                .animal(animal)
                .stateGeneration(state)
                .build();
            log.info("Created new Model3D for animal: {}", animal.getId());
        }
        
        return model3DRepository.save(model3D);
    }
    
    
    /**
     * Extracts URL from JsonNode.
     * 
     * @param jsonNode The JsonNode containing the URL
     * @return The extracted URL or null if not found
     * @author nav
     */
    private String extractUrlFromJsonNode(JsonNode jsonNode) {
        if (jsonNode != null && jsonNode.has("url")) {
            return jsonNode.get("url").asText();
        }
        return null;
    }


}