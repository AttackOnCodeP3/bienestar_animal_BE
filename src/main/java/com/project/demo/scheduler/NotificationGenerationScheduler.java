package com.project.demo.scheduler;

import com.project.demo.logic.constants.scheduling.SchedulerCronConstants;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.notification.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled task that runs daily at midnight to generate user notifications
 * based on registered animals in the system.
 *
 * This class is responsible for orchestrating the execution of the notification
 * generation logic and should delegate business logic to the appropriate service layer.
 *
 * @author your-name
 */
@Component
public class NotificationGenerationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationGenerationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final CommunityAnimalRepository communityAnimalRepository;

    public NotificationGenerationScheduler(
            NotificationRepository notificationRepository,
            CommunityAnimalRepository communityAnimalRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.communityAnimalRepository = communityAnimalRepository;
    }

    /**
     * Executes the scheduled task at midnight (00:00) Costa Rica time.
     * The task checks all users with registered animals and generates the
     * appropriate notifications.
     */
    @Scheduled(cron = SchedulerCronConstants.EVERY_MINUTE, zone = SchedulerCronConstants.ZONE_AMERICA_COSTA_RICA)
    public void runMidnightNotificationTask() {
        //Se
        logger.info("Running midnight notification generation task...{}", LocalDateTime.now());
    }
}
