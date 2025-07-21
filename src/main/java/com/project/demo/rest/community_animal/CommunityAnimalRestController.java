package com.project.demo.rest.community_animal;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.animal_type.AnimalTypeEnum;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.race.RaceRepository;
import com.project.demo.logic.entity.sanitary_control.SanitaryControl;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponse;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseRepository;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlType;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeRepository;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.vaccine.Vaccine;
import com.project.demo.logic.entity.vaccine.VaccineRepository;
import com.project.demo.logic.entity.vaccine_application.VaccineApplication;
import com.project.demo.logic.entity.vaccine_application.VaccineApplicationRepository;
import com.project.demo.rest.community_animal.dto.CreateAnimalRequestDTO;
import com.project.demo.rest.community_animal.dto.SanitaryControlDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/community-animals")
public class CommunityAnimalRestController {

    @Autowired private CommunityAnimalRepository communityAnimalRepository;

    @Autowired private JwtService jwtService;

    @Autowired private SpeciesRepository speciesRepository;

    @Autowired private RaceRepository raceRepository;

    @Autowired private SexRepository sexRepository;

    @Autowired private VaccineRepository vaccineRepository;

    @Autowired private SanitaryControlTypeRepository sanitaryControlTypeRepository;

    @Autowired private SanitaryControlResponseRepository sanitaryControlResponseRepository;

    @Autowired private VaccineApplicationRepository vaccineApplicationRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private AnimalTypeRepository animalTypeRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommunityAnimalRestController.class);


    @PostMapping
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> createCommunityAnimal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateAnimalRequestDTO createAnimalRequestDTO,
            HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Creating new community animal...");

            String email = jwtService.extractUsername(authHeader.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return responseHandler.unauthorized("No se pudo identificar al usuario autenticado.", request);
            }

            Species species = speciesRepository.findById(createAnimalRequestDTO.getSpeciesId()).orElse(null);
            if (species == null) {
                return responseHandler.badRequest("La especie especificada no existe.", request);
            }

            Race race = raceRepository.findById(createAnimalRequestDTO.getRaceId()).orElse(null);
            if (race == null) {
                return responseHandler.badRequest("La raza especificada no existe.", request);
            }

            Sex sex = sexRepository.findById(createAnimalRequestDTO.getSexId()).orElse(null);
            if (sex == null) {
                return responseHandler.badRequest("El sexo especificado no existe.", request);
            }

            var animalType = animalTypeRepository.findByName(AnimalTypeEnum.COMMUNITY_ANIMAL.getName()).orElse(null);
            if (animalType == null) {
                return responseHandler.badRequest("El tipo de animal comunitario no se encuentra registrado.", request);
            }

            if (createAnimalRequestDTO.getBirthDate().isAfter(LocalDate.now())) {
                return responseHandler.badRequest("La fecha de nacimiento no puede estar en el futuro.", request);
            }

            if (createAnimalRequestDTO.getSanitaryControls() != null && !createAnimalRequestDTO.getSanitaryControls().isEmpty()) {
                for (SanitaryControlDTO ctrlDto : createAnimalRequestDTO.getSanitaryControls()) {

                    if (ctrlDto.getSanitaryControlTypeId() == null) {
                        return responseHandler.badRequest(
                                "El tipo de control sanitario es obligatorio.",
                                request
                        );
                    }

                    if (ctrlDto.getSanitaryControlResponseId() == null) {
                        return responseHandler.badRequest(
                                "La respuesta del control sanitario es obligatoria.",
                                request
                        );
                    }

                    boolean productUsedProvided = ctrlDto.getProductUsed() != null && !ctrlDto.getProductUsed().isBlank();
                    boolean dateProvided = ctrlDto.getLastApplicationDate() != null;

                    boolean bothAbsent = !productUsedProvided && !dateProvided;
                    boolean bothPresent = productUsedProvided && dateProvided;

                    if (!bothAbsent && !bothPresent) {
                        return responseHandler.badRequest(
                                "Si se proporciona el producto utilizado o la fecha de aplicación en los controles sanitarios, ambos campos deben estar completos.",
                                request
                        );
                    }
                }
            }

            CommunityAnimal communityAnimal = CommunityAnimal.builder()
                    .name(createAnimalRequestDTO.getName())
                    .weight(createAnimalRequestDTO.getWeight())
                    .birthDate(createAnimalRequestDTO.getBirthDate())
                    .species(species)
                    .race(race)
                    .sex(sex)
                    .latitude(createAnimalRequestDTO.getLatitude())
                    .longitude(createAnimalRequestDTO.getLongitude())
                    .animalType(animalType)
                    .user(user)
                    .build();

            if (createAnimalRequestDTO.getSanitaryControls() != null) {
                List<SanitaryControl> sanitaryControls = new ArrayList<>();
                for (SanitaryControlDTO ctrlDto : createAnimalRequestDTO.getSanitaryControls()) {
                    SanitaryControlType type = sanitaryControlTypeRepository.findById(ctrlDto.getSanitaryControlTypeId()).orElse(null);
                    if (type == null) {
                        return responseHandler.badRequest("El tipo de control sanitario especificado no existe.", request);
                    }

                    SanitaryControlResponse response = sanitaryControlResponseRepository.findById(ctrlDto.getSanitaryControlResponseId()).orElse(null);
                    if (response == null) {
                        return responseHandler.badRequest("La respuesta del control sanitario especificada no existe.", request);
                    }

                    SanitaryControl control = SanitaryControl.builder()
                            .lastApplicationDate(ctrlDto.getLastApplicationDate())
                            .productUsed(ctrlDto.getProductUsed())
                            .sanitaryControlType(type)
                            .sanitaryControlResponse(response)
                            .build();

                    control.setAnimal(communityAnimal);
                    sanitaryControls.add(control);
                }

                communityAnimal.setSanitaryControls(sanitaryControls);
            }

            CommunityAnimal communityAnimalSaved = communityAnimalRepository.save(communityAnimal);

            if (createAnimalRequestDTO.getVaccineApplications() != null) {
                List<VaccineApplication> applications = new ArrayList<>();
                for (var appDTO : createAnimalRequestDTO.getVaccineApplications()) {
                    Vaccine vaccine = vaccineRepository.findById(appDTO.getVaccineId()).orElse(null);
                    if (vaccine == null) {
                        return responseHandler.badRequest("La vacuna especificada no existe.", request);
                    }

                    applications.add(VaccineApplication.builder()
                            .animal(communityAnimalSaved)
                            .vaccine(vaccine)
                            .applicationDate(appDTO.getApplicationDate())
                            .build());
                }

                vaccineApplicationRepository.saveAll(applications);
            }

            return responseHandler.created("Animal comunitario registrado exitosamente.", communityAnimalSaved, request);

        } catch (Exception e) {
            logger.error("Error al registrar el animal comunitario", e);
            return responseHandler.internalError("Se produjo un error inesperado al registrar el animal comunitario: " + e.getMessage(), request);
        }
    }

    /**
     * Returns the list of community animals registered by the currently authenticated user.
     *
     * @param authHeader Authorization header containing the JWT token
     * @param request    HTTP request (used for metadata)
     * @return ResponseEntity containing the list of animals
     */
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAnimals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Fetching community animals for authenticated user");

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<CommunityAnimal> animalsPage = communityAnimalRepository.findByUser_Email(email, pageable);

        Meta meta = PaginationUtils.buildMeta(request, animalsPage);

        return new GlobalResponseHandler().handleResponse(
                "Community animals retrieved successfully",
                animalsPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }
}

