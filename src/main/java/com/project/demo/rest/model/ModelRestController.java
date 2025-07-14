package com.project.demo.rest.model;
import com.project.demo.rest.model.dto.UploadPictureResponseDTO;
import com.project.demo.rest.model.dto.CreateTaskResponseDTO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.*;
import com.project.demo.service.Tripo3DService.Tripo3DService;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequestMapping("/api")

public class ModelRestController {

    private final Tripo3DService tripo3DService;
    

    public ModelRestController(Tripo3DService tripo3DService) {
        this.tripo3DService = tripo3DService;
    }

    /**
     * Endpoint to upload a picture and get a public URL.
     * @param file The image file to upload
     * @return ResponseEntity with success status and public image URL
     * If an error occurs, it returns a 400 status with an error message.
     * @author nav
     * This endpoint allows users to upload an image file, which is then processed
     * to obtain a public URL via Imgur. 
     * * @throws IOException If an error occurs during file reading or upload
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
 * Endpoint to create a task for image to model conversion using v2.5 of the service.
 * This endpoint allows users to create a task for converting an image to a 3D model
 * using version 2.5 of the Tripo3D service.
 * @param imageUrl The public URL of the image to be processed
 * @return ResponseEntity with success status, task ID, and other details
 * If an error occurs, it returns a 400 or 500 status with an error message.
 * * @throws IOException If an error occurs during task creation or response parsing
 *   @author nav
 * */
 
@PostMapping("/createTaskV25")
public ResponseEntity<CreateTaskResponseDTO> createTaskV25(@RequestParam("image_url") String imageUrl) {
    if(imageUrl == null || imageUrl.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(new CreateTaskResponseDTO(false, "image_url is required and cannot be empty", null, null, null,null));
    }
    try {
        String taskResponse = tripo3DService.createImageToModelTaskV25(imageUrl);
        log.info("Task response: " + taskResponse);
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode taskJson = mapper.readTree(taskResponse);
        
        if (taskJson.has("task_id")) {
            String taskId = taskJson.get("task_id").asText();
       JsonNode modelMesh = taskJson.get("model_mesh");
            JsonNode renderedImage = taskJson.get("rendered_image");
            
            return ResponseEntity.ok(new CreateTaskResponseDTO(
                true,
                "Task created successfully",
                taskId,
                imageUrl,
                modelMesh,
                renderedImage
            ));
        } else {
            return ResponseEntity.status(500).body(new CreateTaskResponseDTO(
                false,
                "Task creation failed, no task_id in response",
                null,
                imageUrl,
                null,
                null
            ));
        }
        
    } catch (Exception e) {
       log.error("Error creating task: ", e);
        return ResponseEntity.status(500).body((new CreateTaskResponseDTO(
            false,
            "Error creating task: " + e.getMessage(),
            null,
            imageUrl,
            null,
            null
        )));
    }
}
}
