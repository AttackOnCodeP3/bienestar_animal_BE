package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlType;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeEnum;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(GeneralConstants.SANITARY_CONTROL_TYPE_SEEDER_ORDER)
@Component
public class SanitaryControlTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final SanitaryControlTypeRepository sanitaryControlTypeRepository;
    private final Logger logger = LoggerFactory.getLogger(SanitaryControlTypeSeeder.class);

    public SanitaryControlTypeSeeder(SanitaryControlTypeRepository sanitaryControlTypeRepository) {
        this.sanitaryControlTypeRepository = sanitaryControlTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadSanitaryControlTypes();
    }

    /**
     * Loads predefined sanitary control types into the database if they do not already exist.
     * This method is called when the application context is refreshed.
     * @author dgutierrez
     */
    private void loadSanitaryControlTypes() {
        if (sanitaryControlTypeRepository.count() > 0) {
            logger.info("SanitaryControlTypeSeeder: Sanitary control types already exist, skipping seeding.");
            return;
        }

        for (SanitaryControlTypeEnum typeEnum : SanitaryControlTypeEnum.values()) {
            if (sanitaryControlTypeRepository.findByName(typeEnum.getName()).isEmpty()) {

                var sanitaryControlType = SanitaryControlType.builder()
                        .name(typeEnum.getName())
                        .description(typeEnum.getDescription())
                        .build();

                sanitaryControlTypeRepository.save(sanitaryControlType);
                logger.info("SanitaryControlTypeSeeder: Sanitary control type {} created successfully.", typeEnum.getName());
            }
        }

        logger.info("SanitaryControlTypeSeeder: Sanitary control types seeded successfully.");
    }
}
