package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.municipality.MunicipalityStatus;
import com.project.demo.logic.entity.municipality.MunicipalityStatusEnum;
import com.project.demo.logic.entity.municipality.MunicipalityStatusRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Logger;


/**
 * Seeder for MunicipalityStatus entities.
 * <p>
 * This class listens for the application context refresh event and populates the database
 * with predefined municipality statuses if they do not already exist.
 * @author dgutierrez
 */
@Order(GeneralConstants.MUNICIPALITY_STATUS_SEEDER_ORDER)
@Component
public class MunicipalityStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final MunicipalityStatusRepository municipalityStatusRepository;
    private final Logger logger = Logger.getLogger(MunicipalityStatusSeeder.class.getName());

    public MunicipalityStatusSeeder(MunicipalityStatusRepository statusRepository) {
        this.municipalityStatusRepository = statusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadMunicipalityStatuses();
    }

    private void loadMunicipalityStatuses() {
        if (municipalityStatusRepository.count() > 0) {
            logger.info("MunicipalityStatusSeeder: Statuses already exist, skipping.");
            return;
        }

        for (MunicipalityStatusEnum statusEnum : MunicipalityStatusEnum.values()) {
            Optional<MunicipalityStatus> optionalStatus = municipalityStatusRepository.findByName(statusEnum.getDisplayName());

            if (optionalStatus.isEmpty()) {
                MunicipalityStatus status = MunicipalityStatus.builder()
                        .name(statusEnum.getDisplayName())
                        .description(statusEnum.getDescription())
                        .build();

                municipalityStatusRepository.save(status);
                logger.info("MunicipalityStatusSeeder: Status " + statusEnum.getDisplayName() + " created successfully.");
            }
        }
    }
}
