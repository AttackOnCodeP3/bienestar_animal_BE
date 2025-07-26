package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfiguration;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationEnum;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Order(GeneralConstants.MUNICIPAL_PREVENTIVE_CARE_CONFIGURATION_SEEDER_ORDER)
@Component
public class MunicipalPreventiveCareConfigurationSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final MunicipalPreventiveCareConfigurationRepository configRepository;
    private final MunicipalityRepository municipalityRepository;

    private final Logger logger = LoggerFactory.getLogger(MunicipalPreventiveCareConfigurationSeeder.class);

    public MunicipalPreventiveCareConfigurationSeeder(
            MunicipalPreventiveCareConfigurationRepository configRepository,
            MunicipalityRepository municipalityRepository
    ) {
        this.configRepository = configRepository;
        this.municipalityRepository = municipalityRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadConfigurations();
    }

    private void loadConfigurations() {
        if (configRepository.count() > 0) {
            logger.info("MunicipalPreventiveCareConfigurationSeeder: Configurations already exist, skipping.");
            return;
        }

        Optional<Municipality> optionalMunicipality = municipalityRepository.findByName(GeneralConstants.NAME_OF_MUNICIPALITY_LA_UNION_CARTAGO_SEEDER);
        if (optionalMunicipality.isEmpty()) {
            logger.error("MunicipalPreventiveCareConfigurationSeeder: Municipality 'La Unión-Cartago' not found.");
            return;
        }

        Municipality municipality = optionalMunicipality.get();

        for (MunicipalPreventiveCareConfigurationEnum type : MunicipalPreventiveCareConfigurationEnum.values()) {
            boolean exists = configRepository.existsByMunicipalityAndType(municipality, type);
            if (!exists) {
                MunicipalPreventiveCareConfiguration config = MunicipalPreventiveCareConfiguration.builder()
                        .type(type)
                        .value(3)
                        .municipality(municipality)
                        .build();
                configRepository.save(config);
                logger.info("Seeder: Preventive care config '{}' created for municipality '{}'.", type.name(), municipality.getName());
            } else {
                logger.info("Seeder: Config '{}' already exists for municipality '{}', skipping.", type.name(), municipality.getName());
            }
        }

        logger.info("MunicipalPreventiveCareConfigurationSeeder: Finished seeding configs.");
    }
}