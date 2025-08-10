package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.complaint_state.ComplaintState;
import com.project.demo.logic.entity.complaint_state.ComplaintStateEnum;
import com.project.demo.logic.entity.complaint_state.ComplaintStateRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeder to populate the ComplaintState table using the values defined in ComplaintStateEnum.
 * Skips insertion if records already exist.
 *
 * @author dgutierrez
 */
@Order(GeneralConstants.COMPLAINT_STATE_SEEDER_ORDER)
@Component
public class ComplaintStateSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final ComplaintStateRepository complaintStateRepository;
    private final Logger logger = LoggerFactory.getLogger(ComplaintStateSeeder.class);

    public ComplaintStateSeeder(ComplaintStateRepository complaintStateRepository) {
        this.complaintStateRepository = complaintStateRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadComplaintStates();
    }

    private void loadComplaintStates() {
        if (complaintStateRepository.count() > 0) {
            logger.info("ComplaintStateSeeder: Complaint states already exist, skipping seeding.");
            return;
        }

        for (ComplaintStateEnum stateEnum : ComplaintStateEnum.values()) {
            if (complaintStateRepository.findByName(stateEnum.getName()).isEmpty()) {
                var complaintState = ComplaintState.builder()
                        .name(stateEnum.getName())
                        .description(stateEnum.getDescription())
                        .build();
                complaintStateRepository.save(complaintState);
                logger.info("ComplaintStateSeeder: Complaint state '{}' created successfully.", stateEnum.getName());
            }
        }

        logger.info("ComplaintStateSeeder: Complaint states seeded successfully.");
    }
}
