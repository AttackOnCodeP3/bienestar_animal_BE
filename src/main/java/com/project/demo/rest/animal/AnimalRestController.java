package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.animal.dto.CreateAbandonedAnimalRequestDTO;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/animals")
public class AnimalRestController {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private CantonRepository cantonRepository;

    @Autowired
    private UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(AnimalRestController.class);

    @PostMapping("/abandoned")
    @PreAuthorize("hasRole('CENSISTA_USER')")
    public ResponseEntity<?> createAbandonedAnimal(
            @RequestBody CreateAbandonedAnimalRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        logger.info("Creating abandoned animal record by censista: {}", userDetails.getUsername());

        User censista = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        Canton canton = censista.getMunicipality().getCanton();
        if (canton == null) {
            throw new EntityNotFoundException("Canton not associated to censista's municipality");
        }

        Animal animal = Animal.builder()
                .species(dto.getSpecies())
                .sex(dto.getSex())
                .estimatedAge(dto.getEstimatedAge())
                .physicalCondition(dto.getPhysicalCondition())
                .behavior(dto.getBehavior())
                .district(dto.getDistrict())
                .neighborhood(dto.getNeighborhood())
                .observations(dto.getObservations())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .photoUrl(dto.getPhotoBase64())
                .canton(canton)
                .createdBy(censista)
                .isAbandoned(true)
                .synchronizedFlag(false)
                .build();

        Animal saved = animalRepository.save(animal);

        return new GlobalResponseHandler().handleResponse(
                "Abandoned animal registered successfully",
                saved,
                HttpStatus.CREATED,
                request
        );
    }
}
