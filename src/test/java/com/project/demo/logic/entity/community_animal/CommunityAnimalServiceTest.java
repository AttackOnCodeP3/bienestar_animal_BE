package com.project.demo.logic.entity.community_animal;

import com.project.demo.logic.entity.animal_type.AnimalType;
import com.project.demo.logic.entity.animal_type.AnimalTypeEnum;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.race.RaceRepository;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.vaccine.VaccineRepository;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseRepository;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeRepository;
import com.project.demo.logic.entity.vaccine_application.VaccineApplicationRepository;
import com.project.demo.rest.community_animal.dto.CreateAnimalRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityAnimalServiceTest {

    @Mock private CommunityAnimalRepository communityAnimalRepository;
    @Mock private SpeciesRepository speciesRepository;
    @Mock private RaceRepository raceRepository;
    @Mock private SexRepository sexRepository;
    @Mock private VaccineRepository vaccineRepository;
    @Mock private SanitaryControlTypeRepository sanitaryControlTypeRepository;
    @Mock private SanitaryControlResponseRepository sanitaryControlResponseRepository;
    @Mock private VaccineApplicationRepository vaccineApplicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private AnimalTypeRepository animalTypeRepository;

    @InjectMocks private CommunityAnimalService service;

    private CreateAnimalRequestDTO baseDto;

    @BeforeEach
    void setup() {
        baseDto = new CreateAnimalRequestDTO();
        baseDto.setName("Bobi");
        baseDto.setBirthDate(LocalDate.now().plusDays(1)); // fecha futura para disparar excepción
        baseDto.setSpeciesId(1L);
        baseDto.setRaceId(2L);
        baseDto.setSexId(3L);

        when(userRepository.findByEmail("u@mail.com")).thenReturn(Optional.of(User.builder().id(5L).email("u@mail.com").build()));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(Species.builder().id(1L).build()));
        when(raceRepository.findById(2L)).thenReturn(Optional.of(Race.builder().id(2L).build()));
        when(sexRepository.findById(3L)).thenReturn(Optional.of(Sex.builder().id(3L).build()));
        when(animalTypeRepository.findByName(AnimalTypeEnum.COMMUNITY_ANIMAL.getName()))
                .thenReturn(Optional.of(AnimalType.builder().id(9L).name(AnimalTypeEnum.COMMUNITY_ANIMAL.getName()).build()));
    }

    @Test
    void createCommunityAnimal_futureBirthDate_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCommunityAnimal("u@mail.com", baseDto));

        assertTrue(ex.getMessage().contains("futuro"));

        verify(communityAnimalRepository, never()).save(any());
    }
}
