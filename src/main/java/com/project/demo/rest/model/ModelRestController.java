package com.project.demo.rest.model;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.rest.model.dto.CreateTaskResponseDTO;
import com.project.demo.rest.model.dto.Model3DResponseDTO;
import com.project.demo.rest.model.dto.UploadPictureResponseDTO;
import com.project.demo.service.model.Model3DService;
import com.project.demo.service.model.Tripo3DService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/model3d-animal")
public class ModelRestController {
    
    private final Tripo3DService tripo3DService;
    private final Model3DService model3DService;
    private final AnimalRepository animalRepository;

    public ModelRestController(Tripo3DService tripo3DService,
                              Model3DService model3DService,
                              AnimalRepository animalRepository) {
        this.tripo3DService = tripo3DService;
        this.model3DService = model3DService;
        this.animalRepository = animalRepository;
    }

@GetMapping("/animal/{animalId}")
public ResponseEntity<?> getModel3DByAnimalId(@PathVariable Long animalId) {
    try {
        log.info("Attempting to find 3D model for animal ID: {}", animalId);
        
        Optional<Animal> animalOpt = animalRepository.findById(animalId);
        if (animalOpt.isEmpty()) {
            log.warn("Animal not found with ID: {}", animalId);
            return ResponseEntity.notFound().build();
        }
        
        Optional<Model3DResponseDTO> model3DOptional = model3DService.findByAnimalId(animalId);
        if (model3DOptional.isEmpty()) {
            log.info("No 3D model found for animal ID: {}", animalId);
            return ResponseEntity.notFound().build();
        }
        
        Model3DResponseDTO model3D = model3DOptional.get();
        log.info("Successfully found 3D model for animal ID: {}", animalId);
        
        Meta meta = new Meta("GET", "/model3d-animal/animal/" + animalId);
        
        return new GlobalResponseHandler().handleResponse(
            "3D model retrieved successfully", 
            model3D, 
            HttpStatus.OK, 
            meta
        );
        
    } catch (Exception e) {
        log.error("Error retrieving 3D model for animal ID: {} - Error: {}", animalId, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(createErrorResponse("Error retrieving 3D model: " + e.getMessage(), null));
    }
}
    /**
     * Endpoint to create a task for image to model conversion using v2.5 of the service.
     * This endpoint allows users to create a task for converting an image to a 3D model
     * using version 2.5 of the Tripo3D service.
     * @param imageUrl The public URL of the image to be processed
     * @param animalId The ID of the animal associated with the model
     * @return ResponseEntity with success status, task ID, and other details
     * If an error occurs, it returns a 400 or 500 status with an error message.
     * @throws IOException If an error occurs during task creation or response parsing
     * @author nav
     */
    @PostMapping("/createTaskV25")
    public ResponseEntity<CreateTaskResponseDTO> createTaskV25(
            @RequestParam("image_url") String imageUrl,
            @RequestParam("animal_id") Long animalId) {
        
        ResponseEntity<CreateTaskResponseDTO> validationResponse = validateInput(imageUrl, animalId);
        if (validationResponse != null) {
            return validationResponse;
        }
        
        try {
            Animal animal = findAnimalById(animalId);
            
            String taskResponse = tripo3DService.createImageToModelTaskV25(imageUrl);
            log.info("Task response: " + taskResponse);
            
            return processTaskResponse(taskResponse, imageUrl, animal);
            
        } catch (Exception e) {
            log.error("Error creating task: ", e);
            return handleError(e, imageUrl, animalId);
        }
    }

    /**
     * To upload a picture and get a public URL.
     * @param file The image file to upload
     * @return ResponseEntity with success status and public image URL
     * If an error occurs, it returns a 400 status with an error message.
     * @author nav
     * This endpoint allows users to upload an image file, which is then processed
     * to obtain a public URL via Imgur. 
     * * @throws IOException If an error occurs during file reading or upload
     *  * @author nav
    **/
    @PostMapping("/uploadPicture")
    public ResponseEntity<UploadPictureResponseDTO> uploadPicture(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadPictureResponseDTO(false, "File is required and cannot be empty", null));
        }
        try {
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            String publicImageUrl = tripo3DService.uploadToImgur(tempFile);
            Files.delete(tempFile);
            return ResponseEntity.ok(new UploadPictureResponseDTO(true, "File uploaded successfully", publicImageUrl));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new UploadPictureResponseDTO(false, "Error uploading file: " + e.getMessage(), null));
        }
    }

    /** 
     * Validates input parameters for the createTaskV25 endpoint.
     * @param imageUrl The image URL to validate
     * @param animalId The animal ID to validate
     * @return ResponseEntity with error if validation fails, null if validation passes
     * @author nav
     */
    private ResponseEntity<CreateTaskResponseDTO> validateInput(String imageUrl, Long animalId) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                createErrorResponse("image_url is required and cannot be empty", imageUrl)
            );
        }
        
        if (animalId == null) {
            return ResponseEntity.badRequest().body(
                createErrorResponse("animal_id is required", imageUrl)
            );
        }
        
        return null;
    }

    /**
     * Finds an animal by ID or throws an exception if not found.
     * @param animalId The animal ID to search for
     * @return The found Animal entity
     * @throws RuntimeException if animal is not found
     * @author nav
     */
    private Animal findAnimalById(Long animalId) {
        Optional<Animal> animalOpt = animalRepository.findById(animalId);
        if (animalOpt.isEmpty()) {
            throw new RuntimeException("Animal not found with ID: " + animalId);
        }
        return animalOpt.get();
    }

    /**
     * Processes the task response from the 3D service.
     * @param taskResponse The raw response from the service
     * @param imageUrl The original image URL
     * @param animal The animal entity
     * @return ResponseEntity with the processed result
     *  @author nav
     */
    private ResponseEntity<CreateTaskResponseDTO> processTaskResponse(String taskResponse, String imageUrl, Animal animal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode taskJson = mapper.readTree(taskResponse);
            
            if (taskJson.has("task_id")) {
                return handleSuccessResponse(taskJson, imageUrl, animal);
            } else {
                return handleFailureResponse(imageUrl, animal);
            }
        } catch (Exception e) {
            log.error("Error processing task response: ", e);
            throw new RuntimeException("Error processing task response", e);
        }
    }

    /**
     * Handles successful task creation response.
     * @param taskJson The parsed JSON response
     * @param imageUrl The original image URL
     * @param animal The animal entity
     * @return ResponseEntity with success response
     * @author nav
     */
    private ResponseEntity<CreateTaskResponseDTO> handleSuccessResponse(JsonNode taskJson, String imageUrl, Animal animal) {
        String taskId = taskJson.get("task_id").asText();
        JsonNode modelMesh = taskJson.get("model_mesh");
        JsonNode renderedImage = taskJson.get("rendered_image");
        
        CreateTaskResponseDTO responseDTO = new CreateTaskResponseDTO(
            true,
            "Task created successfully",
            taskId,
            imageUrl,
            modelMesh,
            renderedImage
        );
        
        model3DService.saveModel3DFromTaskResponse(responseDTO, animal);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Handles task creation failure response.
     * @param imageUrl The original image URL
     * @param animal The animal entity
     * @return ResponseEntity with failure response
     * @author nav
     */
    private ResponseEntity<CreateTaskResponseDTO> handleFailureResponse(String imageUrl, Animal animal) {
        CreateTaskResponseDTO errorResponse = createErrorResponse(
            "Task creation failed, no task_id in response", 
            imageUrl
        );
        
        model3DService.saveModel3DFromTaskResponse(errorResponse, animal);
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * Handles exceptions and creates appropriate error responses.
     * @param e The exception that occurred
     * @param imageUrl The original image URL
     * @param animalId The animal ID
     * @return ResponseEntity with error response
     * @author nav
     */
    private ResponseEntity<CreateTaskResponseDTO> handleError(Exception e, String imageUrl, Long animalId) {
        CreateTaskResponseDTO errorResponse = createErrorResponse(
            "Error creating task: " + e.getMessage(), 
            imageUrl
        );
        
        try {
            Optional<Animal> animalOpt = animalRepository.findById(animalId);
            if (animalOpt.isPresent()) {
                model3DService.saveModel3DFromTaskResponse(errorResponse, animalOpt.get());
            }
        } catch (Exception saveException) {
            log.error("Error saving error state: ", saveException);
        }
        
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * Creates a standardized error response DTO.
     * @param message The error message
     * @param imageUrl The original image URL
     * @return CreateTaskResponseDTO with error details
     */
    private CreateTaskResponseDTO createErrorResponse(String message, String imageUrl) {
=        return new CreateTaskResponseDTO(
            false,
            message,
            null,  
            imageUrl,
            null,  
            null   
        );
    }
}