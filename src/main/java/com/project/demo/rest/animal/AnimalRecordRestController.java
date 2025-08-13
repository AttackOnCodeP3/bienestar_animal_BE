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

/**
 * REST controller that manages operations related to animal records.
 * <p>
 * Provides endpoints to retrieve lists of community animals and abandoned animals
 * filtered by the owner's ID.
 * </p>
 *
 * @author @aBlancoC
 */
@RestController
@RequestMapping("/animals/records")
public class AnimalRecordRestController {

    /**
     * Repository for accessing and managing animal data.
     */
    @Autowired
    private AnimalRepository animalRepository;

    /**
     * Retrieves a list of community animals owned by a specific user.
     *
     * @param ownerId The ID of the owner whose community animals will be retrieved.
     * @param request The HTTP request object, used for response metadata.
     * @return A {@link ResponseEntity} containing the list of community animals or
     *         a "not found" message if no animals match the criteria.
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

    /**
     * Retrieves a list of abandoned animals reported by a specific user.
     *
     * @param ownerId The ID of the owner whose abandoned animal reports will be retrieved.
     * @param request The HTTP request object, used for response metadata.
     * @return A {@link ResponseEntity} containing the list of abandoned animals or
     *         a "not found" message if no animals match the criteria.
     */
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
