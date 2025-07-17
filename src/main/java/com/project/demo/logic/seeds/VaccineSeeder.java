package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesEnum;
import com.project.demo.logic.entity.species.SpeciesRepository;
import com.project.demo.logic.entity.vaccine.Vaccine;
import com.project.demo.logic.entity.vaccine.VaccineEnum;
import com.project.demo.logic.entity.vaccine.VaccineRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(GeneralConstants.VACCINE_SEEDER_ORDER)
public class VaccineSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final VaccineRepository vaccineRepository;
    private final SpeciesRepository speciesRepository;
    private final Logger logger = LoggerFactory.getLogger(VaccineSeeder.class);

    public VaccineSeeder(VaccineRepository vaccineRepository, SpeciesRepository speciesRepository) {
        this.vaccineRepository = vaccineRepository;
        this.speciesRepository = speciesRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedVaccines();
    }

    private void seedVaccines() {
        if (vaccineRepository.count() > 0) {
            logger.info("VaccineSeeder: Vaccines already exist, skipping seeding.");
            return;
        }

        Optional<Species> dog = speciesRepository.findByName(SpeciesEnum.DOG.getName());
        Optional<Species> cat = speciesRepository.findByName(SpeciesEnum.CAT.getName());

        if (dog.isEmpty() || cat.isEmpty()) {
            logger.error("VaccineSeeder: Species not found. Ensure SpeciesSeeder runs before this.");
            return;
        }

        Map<VaccineEnum, List<Species>> vacunaEspecieMap = Map.of(
                VaccineEnum.RABIES, List.of(dog.get(), cat.get()),
                VaccineEnum.MULTIPLE, List.of(dog.get()),
                VaccineEnum.QUINTUPLE, List.of(dog.get())
        );

        for (Map.Entry<VaccineEnum, List<Species>> entry : vacunaEspecieMap.entrySet()) {
            VaccineEnum vaccineEnum = entry.getKey();
            List<Species> associatedSpecies = entry.getValue();

            if (vaccineRepository.findByName(vaccineEnum.getName()).isEmpty()) {
                Vaccine vaccine = Vaccine.builder()
                        .name(vaccineEnum.getName())
                        .description(vaccineEnum.getDescription())
                        .build();

                for (Species species : associatedSpecies) {
                    vaccine.getSpecies().add(species);
                }

                vaccineRepository.save(vaccine);
                logger.info("VaccineSeeder: Vaccine '{}' created and linked.", vaccineEnum.getName());
            }
        }

        logger.info("VaccineSeeder: All vaccines seeded successfully.");
    }
}
