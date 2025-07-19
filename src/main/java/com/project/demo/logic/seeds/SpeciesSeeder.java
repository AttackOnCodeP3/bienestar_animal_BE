package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesEnum;
import com.project.demo.logic.entity.species.SpeciesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with predefined species data.
 * This class listens for the application context refresh event and populates the database with species
 * if they do not already exist.
 * @author dgutierrez
 */
@Order(GeneralConstants.SPECIES_SEEDER_ORDER)
@Component
public class SpeciesSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final SpeciesRepository speciesRepository;

    private final Logger logger = LoggerFactory.getLogger(SpeciesSeeder.class);

    public SpeciesSeeder(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedSpecies();
    }

    private void seedSpecies() {
        if (speciesRepository.count() > 0) {
            logger.info("SpeciesSeeder: Species already exist, skipping seeding.");
            return;
        }

        for (SpeciesEnum speciesEnum : SpeciesEnum.values()) {
            if (speciesRepository.findByName(speciesEnum.getName()).isEmpty()) {
                var species = Species.builder()
                        .name(speciesEnum.getName())
                        .description(speciesEnum.getDescription())
                        .build();
                speciesRepository.save(species);
                logger.info("SpeciesSeeder: Species '{}' created successfully.", speciesEnum.getName());
            }
        }
        logger.info("SpeciesSeeder: Species seeded successfully.");
    }
}
