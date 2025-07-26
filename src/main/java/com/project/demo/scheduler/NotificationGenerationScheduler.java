package com.project.demo.scheduler;

import com.project.demo.logic.constants.scheduling.SchedulerCronConstants;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationRepository;
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
    private final MunicipalPreventiveCareConfigurationRepository municipalPreventiveCareConfigurationRepository;

    public NotificationGenerationScheduler(
            NotificationRepository notificationRepository,
            CommunityAnimalRepository communityAnimalRepository,
            MunicipalPreventiveCareConfigurationRepository municipalPreventiveCareConfigurationRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.communityAnimalRepository = communityAnimalRepository;
        this.municipalPreventiveCareConfigurationRepository = municipalPreventiveCareConfigurationRepository;
    }

    /**
     * Executes the scheduled task at midnight (00:00) Costa Rica time.
     * The task checks all users with registered animals and generates the
     * appropriate notifications.
     */
    @Scheduled(cron = SchedulerCronConstants.EVERY_MINUTE, zone = SchedulerCronConstants.ZONE_AMERICA_COSTA_RICA)
    public void runMidnightNotificationTask() {
        //Necesitaria primero consultar todas las municipalidades, luego consultar las configuraciones de cada una
        //y luego consultar los animales de cada una de las municipalidades
        //y generar las notificaciones correspondientes.
        logger.info("Running midnight notification generation task...{}", LocalDateTime.now());
    }
}
