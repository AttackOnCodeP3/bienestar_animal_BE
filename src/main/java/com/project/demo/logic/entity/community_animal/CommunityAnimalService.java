package com.project.demo.logic.entity.community_animal;

import com.project.demo.logic.entity.animal_type.AnimalTypeEnum;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityAnimalService {

    @Autowired private CommunityAnimalRepository communityAnimalRepository;
    @Autowired private SpeciesRepository speciesRepository;
    @Autowired private RaceRepository raceRepository;
    @Autowired private SexRepository sexRepository;
    @Autowired private VaccineRepository vaccineRepository;
    @Autowired private SanitaryControlTypeRepository sanitaryControlTypeRepository;
    @Autowired private SanitaryControlResponseRepository sanitaryControlResponseRepository;
    @Autowired private VaccineApplicationRepository vaccineApplicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AnimalTypeRepository animalTypeRepository;

    /**
     * Creates and persists a new community animal along with its sanitary controls and vaccine applications.
     * <p>
     * This method validates all required data (species, race, sex, etc.) and ensures consistency.
     * It throws exceptions when any referenced entity does not exist or when the input data is invalid.
     * </p>
     *
     * @param email The authenticated user's email registering the animal.
     * @param dto   The request data transfer object containing animal registration details.
     * @return The persisted {@link CommunityAnimal} entity including its associations.
     * @throws IllegalArgumentException If any referenced entity is not found or input data is invalid.
     *
     * @modifiedBy gjimenez - Enhanced logic to allow animal registration for other users (by cedula).
     */
    @Transactional
    public CommunityAnimal createCommunityAnimal(String email, CreateAnimalRequestDTO dto) {

        User user;

        if (dto.getOwnerIdentificationCard() != null && !dto.getOwnerIdentificationCard().isBlank()) {
            user = userRepository.findByIdentificationCard(dto.getOwnerIdentificationCard())
                    .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
        }

        Species species = speciesRepository.findById(dto.getSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException("La especie especificada no existe"));

        Race race = raceRepository.findById(dto.getRaceId())
                .orElseThrow(() -> new IllegalArgumentException("La raza especificada no existe"));

        Sex sex = sexRepository.findById(dto.getSexId())
                .orElseThrow(() -> new IllegalArgumentException("El sexo especificado no existe"));

        var animalType = animalTypeRepository.findByName(AnimalTypeEnum.COMMUNITY_ANIMAL.getName())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de animal comunitario no registrado"));

        if (dto.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede estar en el futuro");
        }

        CommunityAnimal animal = CommunityAnimal.builder()
                .name(dto.getName())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .species(species)
                .race(race)
                .sex(sex)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .animalType(animalType)
                .user(user)
                .build();

        if (dto.getSanitaryControls() != null && !dto.getSanitaryControls().isEmpty()) {
            List<SanitaryControl> sanitaryControls = new ArrayList<>();

            for (SanitaryControlDTO ctrl : dto.getSanitaryControls()) {
                if (ctrl.getSanitaryControlTypeId() == null)
                    throw new IllegalArgumentException("Tipo de control sanitario es obligatorio");

                if (ctrl.getSanitaryControlResponseId() == null)
                    throw new IllegalArgumentException("Respuesta del control sanitario es obligatoria");

                boolean productUsedProvided = ctrl.getProductUsed() != null && !ctrl.getProductUsed().isBlank();
                boolean dateProvided = ctrl.getLastApplicationDate() != null;
                boolean bothAbsent = !productUsedProvided && !dateProvided;
                boolean bothPresent = productUsedProvided && dateProvided;

                if (!bothAbsent && !bothPresent)
                    throw new IllegalArgumentException("Si se proporciona producto o fecha de aplicación, ambos campos deben estar completos");

                SanitaryControlType type = sanitaryControlTypeRepository.findById(ctrl.getSanitaryControlTypeId())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de control sanitario no existe"));

                SanitaryControlResponse response = sanitaryControlResponseRepository.findById(ctrl.getSanitaryControlResponseId())
                        .orElseThrow(() -> new IllegalArgumentException("Respuesta del control sanitario no existe"));

                SanitaryControl sanitaryControl = SanitaryControl.builder()
                        .lastApplicationDate(ctrl.getLastApplicationDate())
                        .productUsed(ctrl.getProductUsed())
                        .sanitaryControlType(type)
                        .sanitaryControlResponse(response)
                        .animal(animal)
                        .build();

                sanitaryControls.add(sanitaryControl);
            }

            animal.setSanitaryControls(sanitaryControls);
        }

        CommunityAnimal savedAnimal = communityAnimalRepository.save(animal);

        if (dto.getVaccineApplications() != null) {
            List<VaccineApplication> applications = new ArrayList<>();
            for (var app : dto.getVaccineApplications()) {
                Vaccine vaccine = vaccineRepository.findById(app.getVaccineId())
                        .orElseThrow(() -> new IllegalArgumentException("Vacuna especificada no existe"));

                applications.add(VaccineApplication.builder()
                        .animal(savedAnimal)
                        .vaccine(vaccine)
                        .applicationDate(app.getApplicationDate())
                        .build());
            }
            vaccineApplicationRepository.saveAll(applications);
        }

        return savedAnimal;
    }

    /**
     * Retrieves the paginated list of community animals registered by a specific user.
     *
     * @param email    The authenticated user's email.
     * @param pageable The pagination information (page number, size, sorting).
     * @return A {@link Page} of {@link CommunityAnimal} entities belonging to the user.
     */
    public Page<CommunityAnimal> getAnimalsByUser(String email, Pageable pageable) {
        return communityAnimalRepository.findByUser_Email(email, pageable);
    }
}
