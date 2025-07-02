package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Order(3)
@Component
public class InterestSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final InterestRepository interestRepository;

    private final Logger logger = Logger.getLogger(InterestSeeder.class.getName());

    public InterestSeeder(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadInterests();
    }

    private void loadInterests() {
        if(interestRepository.count() > 0) {
            logger.info("Interests already exist, skipping seeding.");
            return;
        }

        String[] interests = new String[] {
                "Technology", "Health", "Finance", "Education", "Travel",
                "Food", "Sports", "Entertainment", "Environment", "Politics"
        };

        for (String interest : interests) {
            var newInterest = Interest.builder().name(interest).description(interest).build();
            interestRepository.save(newInterest);
            logger.info("Interest '" + interest + "' created successfully.");
        }
    }
}
