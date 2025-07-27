package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestEnum;
import com.project.demo.logic.entity.interest.InterestRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(GeneralConstants.INTEREST_SEEDER_ORDER)
@Component
public class InterestSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final InterestRepository interestRepository;
    private final Logger logger = LoggerFactory.getLogger(InterestSeeder.class);

    public InterestSeeder(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadInterests();
    }

    private void loadInterests() {
        if (interestRepository.count() > 0) {
            logger.info("InterestSeeder: Interests already exist, skipping seeding.");
            return;
        }

        for (InterestEnum interestEnum : InterestEnum.values()) {
            if (interestRepository.findByName(interestEnum.getName()).isEmpty()) {
                var interest = Interest.builder()
                        .name(interestEnum.getName())
                        .description(interestEnum.getDescription())
                        .build();
                interestRepository.save(interest);
                logger.info("InterestSeeder: Interest '{}' created successfully.", interestEnum.getName());
            }
        }

        logger.info("InterestSeeder: Interests seeded successfully.");
    }
}
