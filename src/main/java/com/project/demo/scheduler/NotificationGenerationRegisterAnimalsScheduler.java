package com.project.demo.scheduler;

import com.project.demo.logic.constants.scheduling.SchedulerCronConstants;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.notification.Notification;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification_status.NotificationStatus;
import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import com.project.demo.logic.entity.notification_type.NotificationType;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler to generate notifications for users without registered animals.
 * <p>
 * This scheduler runs every MIDNIGHT and checks for users with the COMMUNITY_USER role
 * who do not have any registered animals. If such users exist, it sends them a notification
 * reminding them to register their community animals.
 * @author dgutierrez
 */
@Component
public class NotificationGenerationRegisterAnimalsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGenerationRegisterAnimalsScheduler.class);

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationStatusRepository notificationStatusRepository;
    private final CommunityAnimalRepository communityAnimalRepository;

    public NotificationGenerationRegisterAnimalsScheduler(
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            NotificationTypeRepository notificationTypeRepository,
            NotificationStatusRepository notificationStatusRepository,
            CommunityAnimalRepository communityAnimalRepository
    ) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.notificationTypeRepository = notificationTypeRepository;
        this.notificationStatusRepository = notificationStatusRepository;
        this.communityAnimalRepository = communityAnimalRepository;
    }

    @Scheduled(cron = SchedulerCronConstants.EVERY_5_MINUTES, zone = SchedulerCronConstants.ZONE_AMERICA_COSTA_RICA)
    public void notifyUsersWithoutRegisteredAnimals() {
        logger.info("Ejecutando scheduler de usuarios sin animales registrados...");

        List<User> communityUsers = userRepository.findAllByRoleName(RoleEnum.COMMUNITY_USER);

        if (communityUsers.isEmpty()) {
            logger.info("No hay usuarios COMMUNITY sin animales registrados.");
            return;
        }

        NotificationType type = notificationTypeRepository.findByName(NotificationTypeEnum.ANIMAL_REGISTRATION.getName())
                .orElseThrow(() -> new IllegalStateException("Tipo ANIMAL_REGISTRATION no encontrado"));

        NotificationStatus status = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Estado SENT no encontrado"));

        for (User user : communityUsers) {
            if (communityAnimalRepository.existsByUser_Email(user.getEmail())) {
                continue;
            }

            boolean wasRecentlyNotified = notificationRepository.findByUserIdAndNotificationType_Name(user.getId(), type.getName())
                    .stream()
                    .anyMatch(n -> {
                        LocalDate lastIssued = LocalDate.parse(n.getDateIssued());
                        return lastIssued.plusDays(7).isAfter(LocalDate.now());
                    });

            if (wasRecentlyNotified) {
                logger.info("Usuario {} fue notificado hace menos de 7 días. Se omite.", user.getId());
                continue;
            }

            Notification notif = Notification.builder()
                    .user(user)
                    .title("Registra tu(s) animal(es) comunitario(s)")
                    .description("Aún no has registrado animales en el sistema. Hazlo para recibir alertas de salud.")
                    .dateIssued(LocalDate.now().toString())
                    .notificationStatus(status)
                    .notificationType(type)
                    .build();

            notificationRepository.save(notif);
            logger.info("Notificación enviada a usuario {}", user.getId());
        }

        logger.info("Scheduler de usuarios sin animales finalizado.");
    }
}
