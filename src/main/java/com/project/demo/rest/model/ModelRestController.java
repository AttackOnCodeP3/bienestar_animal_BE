package com.project.demo.rest.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public ResponseEntity<Map<String, Object>> uploadPicture(@RequestParam("file") MultipartFile file) {
    Map<String, Object> response = new HashMap<>();
    if (file == null || file.isEmpty()) {
        response.put("success", false);
        response.put("message", "Please upload file");
        return ResponseEntity.badRequest().body(response);
    }
    try {
        Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        String publicImageUrl = tripo3DService.uploadToImgur(tempFile);
        Files.delete(tempFile);
        response.put("success", true);
        response.put("public_image_url", publicImageUrl);
        response.put("message", "Image uploaded successfully and public URL obtained");
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
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
public ResponseEntity<Map<String, Object>> createTaskV25(@RequestParam("image_url") String imageUrl) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "image_url is required and cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        
        String taskResponse = tripo3DService.createImageToModelTaskV25(imageUrl);
        log.info("Task response: " + taskResponse);
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode taskJson = mapper.readTree(taskResponse);
        
        if (taskJson.has("task_id")) {
            String taskId = taskJson.get("task_id").asText();
            response.put("success", true);
            response.put("task_id", taskId);
            response.put("message", "Task created successfully with v2.5");
            response.put("image_url", imageUrl);
            
            if (taskJson.has("model_mesh")) {
                response.put("model_mesh", taskJson.get("model_mesh"));
            }
            if (taskJson.has("rendered_image")) {
                response.put("rendered_image", taskJson.get("rendered_image"));
            }
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Cannot get task_id from response");
            response.put("raw_response", taskJson);
            return ResponseEntity.status(500).body(response);
        }
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error: " + e.getMessage());
        response.put("image_url", imageUrl);
        
        log.error("Error creating v2.5 task for image_url: " + imageUrl);
        log.error("Error details: " + e.getMessage());
        log.error("Error creating task", e);
        
        return ResponseEntity.status(500).body(response);
    }
}
}
