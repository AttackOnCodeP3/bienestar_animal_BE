package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.announcement_state.AnnouncementState;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateEnum;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(GeneralConstants.ANNOUNCEMENT_STATE_SEEDER_ORDER)
@Component
public class AnnouncementStateSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final AnnouncementStateRepository announcementStateRepository;
    private final Logger logger = LoggerFactory.getLogger(AnnouncementStateSeeder.class);

    public AnnouncementStateSeeder(AnnouncementStateRepository announcementStateRepository) {
        this.announcementStateRepository = announcementStateRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadAnnouncementStates();
    }

    /**
     * Loads predefined announcement states into the database if they do not already exist.
     * This method is called when the application context is refreshed.
     * @author dgutierrez
     */
    private void loadAnnouncementStates() {
        if (announcementStateRepository.count() > 0) {
            logger.info("AnnouncementStateSeeder: Announcement states already exist, skipping seeding.");
            return;
        }

        for (AnnouncementStateEnum stateEnum : AnnouncementStateEnum.values()) {
            if (announcementStateRepository.findByName(stateEnum.getName()).isEmpty()) {

                var state = AnnouncementState.builder()
                        .name(stateEnum.getName())
                        .description(stateEnum.getDescription())
                        .build();

                announcementStateRepository.save(state);
                logger.info("AnnouncementStateSeeder: Announcement state {} created successfully.", stateEnum.getName());
            }
        }

        logger.info("AnnouncementStateSeeder: Announcement states seeded successfully.");
    }
}
