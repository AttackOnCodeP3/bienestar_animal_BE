package com.project.demo.rest.animal;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.rest.animal.dto.AnimalRecordDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/animals/records")
public class AnimalRecordRestController {

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping("/{animalId}/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnimalRecordByAnimalAndUser(@PathVariable Long animalId, @PathVariable Long userId, HttpServletRequest request) {

        var globalResponseHandler = new GlobalResponseHandler();

        /*Optional<AnimalRecordDTO> animalOpt = animalRepository.findByIdAndUserId(animalId, userId);
        if (animalOpt.isEmpty()) {

            return globalResponseHandler.notFound(
                    "El animal con ID " + animalId + " y usuario con ID " + userId + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Registro de animal obtenido correctamente",
                animalOpt.get(),
                HttpStatus.OK,
                request
        );*/
        return null;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCommunityAnimalsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        var globalResponseHandler = new GlobalResponseHandler();

        // Replace with actual repository method to fetch community animals by userId
        // Example: List<AnimalRecordDTO> animals = animalRepository.findCommunityAnimalsByUserId(userId);

        List<Animal> communityAnimals = animalRepository.findCommunityAnimalsByUserId(userId);

        // Placeholder response
        if (communityAnimals.isEmpty()) {
            return globalResponseHandler.notFound(
               "No community animals found for user with ID " + userId,
                 request
             );
         }
         return globalResponseHandler.handleResponse(
             "Community animals retrieved successfully",
                 communityAnimals,
             HttpStatus.OK,
             request
        );
    }

}