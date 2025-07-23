package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.AbandonedAnimal;
import com.project.demo.logic.entity.animal.AbandonedAnimalRepository;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.animal.dto.CreateAbandonedAnimalRequestDTO;
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
@RequestMapping("/animals/abandoned")
public class AbandonedAnimalRestController {

    @Autowired private AbandonedAnimalRepository abandonedAnimalRepository;
    @Autowired private SpeciesRepository speciesRepository;
    @Autowired private SexRepository sexRepository;
    @Autowired private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AbandonedAnimalRestController.class);

    /**
     * Registers an abandoned animal with the provided data.
     *
     * @param dto           Request data for abandoned animal
     * @param userDetails   Authenticated user details (must be a census user)
     * @param request       HTTP request (for metadata and logging)
     * @return Created abandoned animal
     */
    @PostMapping
    @PreAuthorize("hasRole('CENSISTA_USER')")
    public ResponseEntity<?> registerAbandonedAnimal(
            @RequestBody CreateAbandonedAnimalRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Registrando animal abandonado por el usuario: {}", userDetails.getUsername());

            User censista = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (censista == null) {
                return responseHandler.unauthorized("Usuario autenticado no encontrado.", request);
            }

            if (censista.getMunicipality() == null || censista.getMunicipality().getCanton() == null) {
                return responseHandler.badRequest("El usuario no tiene asignada una municipalidad o cantón válido.", request);
            }

            Canton canton = censista.getMunicipality().getCanton();

            Species species = speciesRepository.findByName(dto.getSpecies()).orElse(null);
            if (species == null) {
                return responseHandler.badRequest("La especie especificada no existe: " + dto.getSpecies(), request);
            }

            Sex sex = null;
            if (dto.getSex() != null) {
                sex = sexRepository.findByName(dto.getSex()).orElse(null);
                if (sex == null) {
                    return responseHandler.badRequest("El sexo especificado no existe: " + dto.getSex(), request);
                }
            }

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

            logger.info("Animal abandonado registrado con ID: {}", saved.getId());

            return responseHandler.created("Animal abandonado registrado exitosamente.", saved, request);

        } catch (Exception e) {
            logger.error("Error al registrar animal abandonado", e);
            return responseHandler.internalError("Ocurrió un error al registrar el animal abandonado.", request);
        }
    }
}