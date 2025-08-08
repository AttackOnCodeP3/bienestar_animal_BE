package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.notification_type.NotificationType;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeder for NotificationType entities.
 * This class populates the NotificationType table in the database
 * with predefined types if they do not already exist.
 * It implements ApplicationListener to run on application context refresh,
 * and uses @Order to define its execution order.
 * @author dgutierrez
 */
@Order(GeneralConstants.NOTIFICATION_TYPE_SEEDER_ORDER)
@Component
public class NotificationTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final NotificationTypeRepository notificationTypeRepository;
    private final Logger logger = LoggerFactory.getLogger(NotificationTypeSeeder.class);

    /**
     * Constructor for NotificationTypeSeeder.
     * @param notificationTypeRepository The repository for NotificationType entities.
     */
    public NotificationTypeSeeder(NotificationTypeRepository notificationTypeRepository) {
        this.notificationTypeRepository = notificationTypeRepository;
    }

    /**
     * This method is called when the application context is refreshed.
     * It triggers the loading of notification types into the database.
     * @param event The ContextRefreshedEvent.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadNotificationTypes();
    }

    /**
     * Loads predefined notification types into the database if they do not already exist.
     * This method checks if any types are present before attempting to seed to prevent duplicates.
     */
    private void loadNotificationTypes() {
        if (notificationTypeRepository.count() > 0) {
            logger.info("NotificationTypeSeeder: Notification types already exist, skipping seeding.");
            return;
        }

        for (NotificationTypeEnum typeEnum : NotificationTypeEnum.values()) {
            if (notificationTypeRepository.findByName(typeEnum.getName()).isEmpty()) {
                var notificationType = NotificationType.builder()
                        .name(typeEnum.getName())
                        .description(typeEnum.getDescription())
                        .build();

                notificationTypeRepository.save(notificationType);
                logger.info("NotificationTypeSeeder: Notification type {} created successfully.", typeEnum.getName());
            }
        }

        logger.info("NotificationTypeSeeder: Notification types seeded successfully.");
    }
}

