package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.state.StateGeneration;
import com.project.demo.logic.entity.state.StateGenerationRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/**
 * Seeder for StateGeneration entities.
 * <p>
 * This class listens for the application context refresh event and populates the database
 * with predefined state generation statuses if they do not already exist.
 * @author nav
 */
@Order(GeneralConstants.STATE_GENERATION_SEEDER_ORDER)
@Component
public class StateSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final StateGenerationRepository stateGenerationRepository;
    private final Logger logger = Logger.getLogger(StateSeeder.class.getName());

    public StateSeeder(StateGenerationRepository stateGenerationRepository) {
        this.stateGenerationRepository = stateGenerationRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadStateGenerations();
    }

    private void loadStateGenerations() {
        if (stateGenerationRepository.count() > 0) {
            logger.info("StateGeneration already exist, skipping seeding.");
            return;
        }

        List<StateGeneration> states = List.of(
                StateGeneration.builder()
                        .name("Pendiente")
                        .description("El modelo 3D está pendiente de generación")
                        .build(),
                StateGeneration.builder()
                        .name("Generado")
                        .description("El modelo 3D ha sido generado exitosamente")
                        .build(),
                StateGeneration.builder()
                        .name("Error")
                        .description("Error durante la generación del modelo 3D")
                        .build()
        );

        stateGenerationRepository.saveAll(states);
        
        states.forEach(state -> {
            logger.info("StateGeneration '" + state.getName() + "' created successfully.");
        });
        
        logger.info("StateGeneration seeded successfully: Pendiente, Generado, Error");
    }
}