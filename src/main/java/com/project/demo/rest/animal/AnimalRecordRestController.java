package com.project.demo.rest.animal;
import com.project.demo.logic.entity.animal.AbandonedAnimal;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST controller for handling animal record operations.
 * <p>
 * Provides endpoints to retrieve community animals by owner ID.
 * </p>
 *
 * @author @aBlancoC
 */
@RestController
@RequestMapping("/animals/records")
public class AnimalRecordRestController {

    @Autowired
    private AnimalRepository animalRepository;

    /**
     * Retrieves a list of community animals associated with a specific owner.
     *
     * @param ownerId the ID of the animal owner
     * @param request the HTTP servlet request
     * @return a {@link ResponseEntity} containing the list of animals or a not found response
     */
    @GetMapping("/community")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCommunityAnimalsByOwnerId(
            @RequestParam Long ownerId,
            HttpServletRequest request) {
        var globalResponseHandler = new GlobalResponseHandler();

        List<Animal> communityAnimals = animalRepository.findCommunityAnimalsByUserId(ownerId);

        if (communityAnimals.isEmpty()) {
            return globalResponseHandler.notFound(
                    "No community animals found for owner with ID " + ownerId,
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

    @GetMapping("/abandoned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAbandonedAnimalsByOwnerId(
            @RequestParam Long ownerId,
            HttpServletRequest request) {
        var globalResponseHandler = new GlobalResponseHandler();

        List<AbandonedAnimal> abandonedAnimals = animalRepository.findAbandonedAnimalsByUserId(ownerId);

        if (abandonedAnimals.isEmpty()) {
            return globalResponseHandler.notFound(
                    "No abandoned animals found for owner with ID " + ownerId,
                    request
            );
        }
        return globalResponseHandler.handleResponse(
                "Abandoned animals retrieved successfully",
                abandonedAnimals,
                HttpStatus.OK,
                request
        );
    }

}

