package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.animal_type.AnimalType;
import com.project.demo.logic.entity.animal_type.AnimalTypeEnum;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeder class to initialize animal types in the database.
 * <p>
 * This class listens for the application context refresh event and populates the database
 * with predefined animal types if they do not already exist.
 * @author dgutierrez
 */
@Order(GeneralConstants.ANIMAL_TYPE_SEEDER_ORDER)
@Component
public class AnimalTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final AnimalTypeRepository animalTypeRepository;

    private final Logger logger = LoggerFactory.getLogger(AnimalTypeSeeder.class);

    public AnimalTypeSeeder(AnimalTypeRepository animalTypeRepository) {
        this.animalTypeRepository = animalTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadAnimalTypes();
    }

    private void loadAnimalTypes() {
        if (animalTypeRepository.count() > 0) {
            logger.info("AnimalTypeSeeder: Animal types already exist, skipping seeding.");
            return;
        }

        for (AnimalTypeEnum animalTypeEnum : AnimalTypeEnum.values()) {
            if (animalTypeRepository.findByName(animalTypeEnum.getName()).isEmpty()) {
                var animalType = AnimalType.builder()
                        .name(animalTypeEnum.getName())
                        .description(animalTypeEnum.getDescription())
                        .build();

                animalTypeRepository.save(animalType);
                logger.info("AnimalTypeSeeder: Animal type '{}' created successfully.", animalTypeEnum.getName());
            }
        }

        logger.info("AnimalTypeSeeder: Animal types seeded successfully.");
    }
}
