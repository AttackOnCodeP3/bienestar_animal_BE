package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexEnum;
import com.project.demo.logic.entity.sex.SexRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Seeder class to populate the database with genders
 * @author dgutierrez
 */
@Order(GeneralConstants.SEX_SEEDER_ORDER)
@Component
public class SexSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final SexRepository sexRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public SexSeeder(SexRepository sexRepository) {
        this.sexRepository = sexRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadSexes();
    }

    private void loadSexes() {
        if (sexRepository.count() > 0) {
            log.info("SexSeeder: Sexes already exist, skipping seeding.");
            return;
        }

        for (SexEnum sexEnum : SexEnum.values()) {
            Optional<Sex> optionalSex = sexRepository.findByName(sexEnum.getName());
            if(optionalSex.isEmpty()) {
                Sex sexToCreate = Sex.builder().name(sexEnum.getName()).build();
                sexRepository.save(sexToCreate);
                log.info("SexSeeder: Sex '{}' created successfully.", sexEnum.getName());
            }
        }
    }
}
