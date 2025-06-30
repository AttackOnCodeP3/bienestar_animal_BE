package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Logger;

@Order(3)
@Component
public class MunicipalitySeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final MunicipalityRepository municipalityRepository;
    private final CantonRepository cantonRepository;

    private final Logger logger = Logger.getLogger(MunicipalitySeeder.class.getName());

    public MunicipalitySeeder(
            MunicipalityRepository municipalityRepository,
            CantonRepository cantonRepository
    ) {
        this.municipalityRepository = municipalityRepository;
        this.cantonRepository = cantonRepository;
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

        if (optionalCanton.isEmpty()) {
            logger.warning("MunicipalitySeeder: Canton with ID 1 not found, cannot create municipality.");
            return;
        }

        var municipality = Municipality.builder().id(1L)
                .name("La Unión-Cartago")
                .email("comunicaciones@munilaunion.cl")
                .phone("2274-5000")
                .address("Provincia de Cartago, Tres Rios, 30301")
                .canton(optionalCanton.get())
                .build();

        municipalityRepository.save(municipality);

        logger.info("MunicipalitySeeder:" + municipality.getName() + " created successfully.");
    }
}
