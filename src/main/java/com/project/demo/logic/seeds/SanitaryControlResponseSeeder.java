package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponse;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseEnum;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder class to initialize sanitary control responses in the database.
 * <p>
 * This class listens for the application context refresh event and populates the database
 * with predefined sanitary control responses (Sí, No, No sé) if they do not already exist.
 * </p>
 *
 * @author dgutierrez
 */
@Order(GeneralConstants.SANITARY_CONTROL_RESPONSE_SEEDER_ORDER)
@Component
public class SanitaryControlResponseSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final SanitaryControlResponseRepository sanitaryControlResponseRepository;

    private final Logger logger = LoggerFactory.getLogger(SanitaryControlResponseSeeder.class);

    public SanitaryControlResponseSeeder(SanitaryControlResponseRepository sanitaryControlResponseRepository) {
        this.sanitaryControlResponseRepository = sanitaryControlResponseRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadSanitaryControlResponses();
    }

    private void loadSanitaryControlResponses() {
        if (sanitaryControlResponseRepository.count() > 0) {
            logger.info("SanitaryControlResponseSeeder: Responses already exist, skipping seeding.");
            return;
        }

        for (SanitaryControlResponseEnum responseEnum : SanitaryControlResponseEnum.values()) {
            if (sanitaryControlResponseRepository.findByName(responseEnum.name()).isEmpty()) {
                var response = SanitaryControlResponse.builder()
                        .name(responseEnum.getName())
                        .description(responseEnum.getDescription())
                        .build();

                sanitaryControlResponseRepository.save(response);
                logger.info("SanitaryControlResponseSeeder: Response '{}' created successfully.", responseEnum.getName());
            }
        }

        logger.info("SanitaryControlResponseSeeder: All sanitary control responses seeded successfully.");
    }
}
