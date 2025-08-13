package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.complaint_type.ComplaintType;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeEnum;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeder to populate the ComplaintType table using the values defined in ComplaintTypeEnum.
 * Skips insertion if records already exist.
 *
 * @author dgutierrez
 */
@Order(GeneralConstants.COMPLAINT_TYPE_SEEDER_ORDER)
@Component
public class ComplaintTypesSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final ComplaintTypeRepository complaintTypeRepository;
    private final Logger logger = LoggerFactory.getLogger(ComplaintTypesSeeder.class);

    public ComplaintTypesSeeder(ComplaintTypeRepository complaintTypeRepository) {
        this.complaintTypeRepository = complaintTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadComplaintTypes();
    }

    private void loadComplaintTypes() {
        if (complaintTypeRepository.count() > 0) {
            logger.info("ComplaintTypesSeeder: Complaint types already exist, skipping seeding.");
            return;
        }

        for (ComplaintTypeEnum typeEnum : ComplaintTypeEnum.values()) {
            if (complaintTypeRepository.findByName(typeEnum.getName()).isEmpty()) {
                var complaintType = ComplaintType.builder()
                        .name(typeEnum.getName())
                        .description(typeEnum.getDescription())
                        .build();
                complaintTypeRepository.save(complaintType);
                logger.info("ComplaintTypesSeeder: Complaint type '{}' created successfully.", typeEnum.getName());
            }
        }

        logger.info("ComplaintTypesSeeder: Complaint types seeded successfully.");
    }
}
