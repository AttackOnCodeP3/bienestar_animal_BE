package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.municipality.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Logger;

@Order(GeneralConstants.MUNICIPALITY_SEEDER_ORDER)
@Component
public class MunicipalitySeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final MunicipalityRepository municipalityRepository;
    private final CantonRepository cantonRepository;
    private final MunicipalityStatusRepository municipalityStatusRepository;

    private final Logger logger = Logger.getLogger(MunicipalitySeeder.class.getName());

    public MunicipalitySeeder(
            MunicipalityRepository municipalityRepository,
            CantonRepository cantonRepository,
            MunicipalityStatusRepository municipalityStatusRepository
    ) {
        this.municipalityRepository = municipalityRepository;
        this.cantonRepository = cantonRepository;
        this.municipalityStatusRepository = municipalityStatusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadMunicipalities();
    }

    private void loadMunicipalities() {
        if (municipalityRepository.count() > 0) {
            logger.info("MunicipalitySeeder: Municipalities already exist, skipping seeding.");
            return;
        }

        Optional<Canton> optionalCanton = cantonRepository.findById(1L);
        Optional<MunicipalityStatus> optionalStatus = municipalityStatusRepository.findByName(MunicipalityStatusEnum.ACTIVE.getDisplayName());

        if (optionalCanton.isEmpty()) {
            logger.warning("MunicipalitySeeder: Canton with ID 1 not found, cannot create municipality.");
            return;
        }

        if (optionalStatus.isEmpty()) {
            logger.warning("MunicipalitySeeder: Status 'ACTIVE' not found, cannot create municipality.");
            return;
        }

        Municipality municipality = Municipality.builder()
                .name("La Unión-Cartago")
                .email("comunicaciones@munilaunion.cl")
                .phone("2274-5000")
                .address("Provincia de Cartago, Tres Rios, 30301")
                .canton(optionalCanton.get())
                .status(optionalStatus.get())
                .build();

        municipalityRepository.save(municipality);

        logger.info("MunicipalitySeeder: " + municipality.getName() + " created successfully.");
    }
}
