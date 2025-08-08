package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.notification_status.NotificationStatus;
import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeder for NotificationStatus entities.
 * This class populates the NotificationStatus table in the database
 * with predefined statuses if they do not already exist.
 * It implements ApplicationListener to run on application context refresh,
 * and uses @Order to define its execution order.
 * @author dgutierrez
 */
@Order(GeneralConstants.NOTIFICATION_STATUS_SEEDER_ORDER)
@Component
public class NotificationStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final NotificationStatusRepository notificationStatusRepository;
    private final Logger logger = LoggerFactory.getLogger(NotificationStatusSeeder.class);

    /**
     * Constructor for NotificationStatusSeeder.
     * @param notificationStatusRepository The repository for NotificationStatus entities.
     */
    public NotificationStatusSeeder(NotificationStatusRepository notificationStatusRepository) {
        this.notificationStatusRepository = notificationStatusRepository;
    }

    /**
     * This method is called when the application context is refreshed.
     * It triggers the loading of notification statuses into the database.
     * @param event The ContextRefreshedEvent.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadNotificationStatuses();
    }

    /**
     * Loads predefined notification statuses into the database if they do not already exist.
     * This method checks if any statuses are present before attempting to seed to prevent duplicates.
     */
    private void loadNotificationStatuses() {
        if (notificationStatusRepository.count() > 0) {
            logger.info("NotificationStatusSeeder: Notification statuses already exist, skipping seeding.");
            return;
        }

        for (NotificationStatusEnum statusEnum : NotificationStatusEnum.values()) {
            if (notificationStatusRepository.findByName(statusEnum.getName()).isEmpty()) {
                var notificationStatus = NotificationStatus.builder()
                        .name(statusEnum.getName())
                        .description(statusEnum.getDescription())
                        .build();

                notificationStatusRepository.save(notificationStatus);
                logger.info("NotificationStatusSeeder: Notification status {} created successfully.", statusEnum.getName());
            }
        }

        logger.info("NotificationStatusSeeder: Notification statuses seeded successfully.");
    }
}
