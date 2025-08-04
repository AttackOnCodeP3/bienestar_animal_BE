package com.project.demo.rest.animal;
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

@RestController
@RequestMapping("/animals/records")
public class AnimalRecordRestController {

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCommunityAnimalsByOwnerId(@RequestParam Long ownerId, HttpServletRequest request) {
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

}