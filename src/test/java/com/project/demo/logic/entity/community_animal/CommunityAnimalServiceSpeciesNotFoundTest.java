package com.project.demo.logic.entity.community_animal;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.community_animal.dto.CreateAnimalRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityAnimalServiceSpeciesNotFoundTest {

    @Mock private CommunityAnimalRepository communityAnimalRepository;
    @Mock private com.project.demo.logic.entity.species.SpeciesRepository speciesRepository;
    @Mock private com.project.demo.logic.entity.race.RaceRepository raceRepository;
    @Mock private com.project.demo.logic.entity.sex.SexRepository sexRepository;
    @Mock private com.project.demo.logic.entity.vaccine.VaccineRepository vaccineRepository;
    @Mock private com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeRepository sanitaryControlTypeRepository;
    @Mock private com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseRepository sanitaryControlResponseRepository;
    @Mock private com.project.demo.logic.entity.vaccine_application.VaccineApplicationRepository vaccineApplicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private com.project.demo.logic.entity.animal_type.AnimalTypeRepository animalTypeRepository;

    @InjectMocks private CommunityAnimalService service;

    @Test
    void whenSpeciesMissing_thenThrows() {
        CreateAnimalRequestDTO dto = new CreateAnimalRequestDTO();
        dto.setSpeciesId(11L);
        when(userRepository.findByEmail("u@mail.com")).thenReturn(Optional.of(User.builder().id(1L).email("u@mail.com").build()));
        when(speciesRepository.findById(11L)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCommunityAnimal("u@mail.com", dto));

        org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("La especie especificada no existe");
    }
}
