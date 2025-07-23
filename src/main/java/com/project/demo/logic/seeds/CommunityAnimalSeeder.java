package com.project.demo.logic.seeds;
import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.race.RaceRepository;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Order(GeneralConstants.COMMUNITY_ANIMAL_SEEDER_ORDER)
@Component
public class CommunityAnimalSeeder implements CommandLineRunner {

    private final CommunityAnimalRepository communityAnimalRepository;
    private final UserRepository userRepository;
    private final SpeciesRepository speciesRepository;
    private final RaceRepository raceRepository;
    private final SexRepository sexRepository;

    public CommunityAnimalSeeder(
            CommunityAnimalRepository communityAnimalRepository,
            UserRepository userRepository,
            SpeciesRepository speciesRepository,
            RaceRepository raceRepository,
            SexRepository sexRepository) {
        this.communityAnimalRepository = communityAnimalRepository;
        this.userRepository = userRepository;
        this.speciesRepository = speciesRepository;
        this.raceRepository = raceRepository;
        this.sexRepository = sexRepository;
    }

    @Override
    public void run(String... args) {
        if (communityAnimalRepository.count() > 0) return;

        User user = userRepository.findById(4L).orElse(null);
        Species species = speciesRepository.findAll().stream().findFirst().orElse(null);
        Race race = raceRepository.findAll().stream().findFirst().orElse(null);
        Sex sex = sexRepository.findAll().stream().findFirst().orElse(null);

        if (user == null || species == null || race == null || sex == null ) return;

        CommunityAnimal animal = CommunityAnimal.builder()
                .name("Maxwell")
                .weight(12.5)
                .species(species)
                .race(race)
                .sex(sex)
                .birthDate(LocalDate.of(2022, 1, 1))
                .latitude(9.9)
                .longitude(-84.0)
                .user(user)
                .build();

        communityAnimalRepository.save(animal);
    }
}