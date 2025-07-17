package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.*;
import com.project.demo.logic.entity.animal_type.AnimalType;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.animal.dto.CreateAbandonedAnimalRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/animals/abandoned")
public class AbandonedAnimalRestController {

    @Autowired
    private AbandonedAnimalRepository abandonedAnimalRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private SexRepository sexRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimalTypeRepository animalTypeRepository;

    @PostMapping
    @PreAuthorize("hasRole('CENSISTA_USER')")
    public ResponseEntity<?> registerAbandonedAnimal(
            @RequestBody CreateAbandonedAnimalRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        User censista = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Canton canton = censista.getMunicipality().getCanton();

        Species species = speciesRepository.findByName(dto.getSpecies())
                .orElseThrow(() -> new EntityNotFoundException("Species not found: " + dto.getSpecies()));

        Sex sex = dto.getSex() != null ? sexRepository.findByName(dto.getSex()).orElse(null) : null;

        AnimalType abandonedType = animalTypeRepository.findByName("Animal abandonado")
                .orElseThrow(() -> new EntityNotFoundException("AnimalType 'Animal abandonado' not found"));

        AbandonedAnimal animal = AbandonedAnimal.builder()
                .species(species)
                .sex(sex)
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
                .synchronizedFlag(false)
                .isAbandoned(true)
                .build();

        AbandonedAnimal saved = abandonedAnimalRepository.save(animal);

        return new GlobalResponseHandler().handleResponse(
                "Abandoned animal registered successfully",
                saved,
                HttpStatus.CREATED,
                request
        );
    }
}
