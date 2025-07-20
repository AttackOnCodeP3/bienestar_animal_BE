package com.project.demo.rest.community_animal;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.race.RaceRepository;
import com.project.demo.logic.entity.sanitary_control.SanitaryControl;
import com.project.demo.logic.entity.sanitary_control.SanitaryControlRepository;
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

    @Autowired private SanitaryControlRepository sanitaryControlRepository;

    @Autowired private SanitaryControlTypeRepository sanitaryControlTypeRepository;

    @Autowired private SanitaryControlResponseRepository sanitaryControlResponseRepository;

    @Autowired private VaccineApplicationRepository vaccineApplicationRepository;

    @Autowired private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommunityAnimalRestController.class);

    @PostMapping
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> createCommunityAnimal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateAnimalRequestDTO dto,
            HttpServletRequest request) {
        try {
            logger.info("Creating new community animal...");

            String email = jwtService.extractUsername(authHeader.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            Species species = speciesRepository.findById(dto.getSpeciesId())
                    .orElseThrow(() -> new RuntimeException("Species not found"));

            Race race = raceRepository.findById(dto.getRaceId())
                    .orElseThrow(() -> new RuntimeException("Race not found"));

            Sex sex = sexRepository.findById(dto.getSexId())
                    .orElseThrow(() -> new RuntimeException("Sex not found"));

            CommunityAnimal animal = CommunityAnimal.builder()
                    .name(dto.getName())
                    .weight(dto.getWeight())
                    .birthDate(dto.getBirthDate())
                    .species(species)
                    .race(race)
                    .sex(sex)
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .user(user)
                    .build();

            if (dto.getSanitaryControls() != null) {
                List<SanitaryControl> sanitaryControls = dto.getSanitaryControls().stream().map(ctrlDto -> {
                    SanitaryControlType type = sanitaryControlTypeRepository.findById(ctrlDto.getSanitaryControlTypeId())
                            .orElseThrow(() -> new RuntimeException("SanitaryControlType not found"));
                    SanitaryControlResponse response = sanitaryControlResponseRepository.findById(ctrlDto.getSanitaryControlResponseId())
                            .orElseThrow(() -> new RuntimeException("SanitaryControlResponse not found"));

                    return SanitaryControl.builder()
                            .lastApplicationDate(ctrlDto.getLastApplicationDate())
                            .productUsed(ctrlDto.getProductUsed())
                            .sanitaryControlType(type)
                            .sanitaryControlResponse(response)
                            .animal(animal)
                            .build();
                }).toList();

                animal.setSanitaryControls(sanitaryControls);
            }

            CommunityAnimal savedAnimal = communityAnimalRepository.save(animal);

            if (dto.getVaccineApplications() != null) {
                List<VaccineApplication> applications = dto.getVaccineApplications().stream().map(appDTO -> {
                    Vaccine vaccine = vaccineRepository.findById(appDTO.getVaccineId())
                            .orElseThrow(() -> new RuntimeException("Vaccine not found"));
                    return VaccineApplication.builder()
                            .animal(savedAnimal)
                            .vaccine(vaccine)
                            .applicationDate(appDTO.getApplicationDate())
                            .build();
                }).toList();

                vaccineApplicationRepository.saveAll(applications);
            }

            return new GlobalResponseHandler().handleResponse(
                    "Community animal created successfully",
                    savedAnimal,
                    HttpStatus.CREATED,
                    request
            );

        } catch (Exception e) {
            logger.error("Error creating community animal", e);
            return new GlobalResponseHandler().handleResponse(
                    "Error creating community animal: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request
            );
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

