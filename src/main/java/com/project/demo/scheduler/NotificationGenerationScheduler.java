package com.project.demo.scheduler;

import com.project.demo.logic.constants.scheduling.SchedulerCronConstants;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.district.District;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfiguration;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationEnum;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.notification.Notification;
import com.project.demo.logic.entity.notification.NotificationRepository;
import com.project.demo.logic.entity.notification_status.NotificationStatus;
import com.project.demo.logic.entity.notification_status.NotificationStatusRepository;
import com.project.demo.logic.entity.notification_status.NotificationStatusEnum;
import com.project.demo.logic.entity.notification_type.NotificationType;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.notification_type.NotificationTypeRepository;
import com.project.demo.logic.entity.sanitary_control.SanitaryControl;
import com.project.demo.logic.entity.sanitary_control.SanitaryControlRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.vaccine_application.VaccineApplication;
import com.project.demo.logic.entity.vaccine_application.VaccineApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Scheduled task that runs daily at midnight to generate user notifications
 * based on registered animals in the system.
 *
 * This class is responsible for orchestrating the execution of the notification
 * generation logic and should delegate business logic to the appropriate service layer.
 *
 * @author dgutierrez
 */
@Component
public class NotificationGenerationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationGenerationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationStatusRepository notificationStatusRepository;
    private final CommunityAnimalRepository communityAnimalRepository;
    private final MunicipalPreventiveCareConfigurationRepository configurationRepository;
    private final MunicipalityRepository municipalityRepository;
    private final SanitaryControlRepository sanitaryControlRepository;
    private final VaccineApplicationRepository vaccineApplicationRepository;

    public NotificationGenerationScheduler(
            NotificationRepository notificationRepository,
            NotificationTypeRepository notificationTypeRepository,
            NotificationStatusRepository notificationStatusRepository,
            CommunityAnimalRepository communityAnimalRepository,
            MunicipalPreventiveCareConfigurationRepository configurationRepository,
            MunicipalityRepository municipalityRepository,
            SanitaryControlRepository sanitaryControlRepository,
            VaccineApplicationRepository vaccineApplicationRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationTypeRepository = notificationTypeRepository;
        this.notificationStatusRepository = notificationStatusRepository;
        this.communityAnimalRepository = communityAnimalRepository;
        this.configurationRepository = configurationRepository;
        this.municipalityRepository = municipalityRepository;
        this.sanitaryControlRepository = sanitaryControlRepository;
        this.vaccineApplicationRepository = vaccineApplicationRepository;
    }

    @Scheduled(cron = SchedulerCronConstants.EVERY_30_SECONDS, zone = SchedulerCronConstants.ZONE_AMERICA_COSTA_RICA)
    public void runMidnightNotificationTask() {
        logger.info("Ejecutando scheduler de generación de notificaciones: {}", LocalDateTime.now());

        List<Municipality> municipalities = municipalityRepository.findAll();
        if (municipalities.isEmpty()) {
            logger.warn("No hay municipalidades registradas. El scheduler termina.");
            return;
        }

        municipalities.forEach(this::processMunicipality);
    }

    private void processMunicipality(Municipality municipality) {
        logger.info("Procesando municipalidad: {}", municipality.getName());

        List<MunicipalPreventiveCareConfiguration> configs = configurationRepository.findAll().stream()
                .filter(conf -> conf.getMunicipality().getId().equals(municipality.getId()))
                .toList();

        if (configs.isEmpty()) {
            logger.warn("No hay configuraciones para la municipalidad {}", municipality.getName());
            return;
        }

        List<CommunityAnimal> animals = communityAnimalRepository.findAll().stream()
                .filter(animal -> Optional.ofNullable(animal.getUser())
                        .map(User::getMunicipality)
                        .map(m -> m.getId().equals(municipality.getId()))
                        .orElse(false))
                .toList();

        animals.forEach(animal -> processAnimal(animal, configs));
    }

    private void processAnimal(CommunityAnimal animal, List<MunicipalPreventiveCareConfiguration> configs) {
        logger.info("Procesando animal con ID: {} y nombre: {}", animal.getId(), animal.getName());
        for (MunicipalPreventiveCareConfiguration config : configs) {
            boolean needsNotification;

            if (config.getType() == MunicipalPreventiveCareConfigurationEnum.VACCINATION) {
                needsNotification = evaluateVaccination(animal, config);
            } else {
                needsNotification = evaluateSanitaryControl(animal, config);
            }

            if (needsNotification) {
                generateNotification(animal, config);
            }
        }
    }

    private boolean evaluateVaccination(CommunityAnimal animal, MunicipalPreventiveCareConfiguration config) {
        LocalDate threshold = LocalDate.now().minusMonths(config.getValue());

        List<VaccineApplication> applications = vaccineApplicationRepository.findAll().stream()
                .filter(va -> va.getAnimal().getId().equals(animal.getId()))
                .filter(va -> va.getApplicationDate() != null && va.getApplicationDate().isAfter(threshold))
                .toList();

        boolean isValid = !applications.isEmpty();
        logger.debug("💉 Evaluando vacunación para animal {} → {}", animal.getId(), isValid ? "al día" : "atrasado");
        return !isValid;
    }

    private boolean evaluateSanitaryControl(CommunityAnimal animal, MunicipalPreventiveCareConfiguration config) {
        LocalDate threshold = LocalDate.now().minusMonths(config.getValue());

        List<SanitaryControl> controls = sanitaryControlRepository.findAll().stream()
                .filter(sc -> sc.getAnimal().getId().equals(animal.getId()))
                .filter(sc -> sc.getSanitaryControlType().getName().equalsIgnoreCase(config.getType().name()))
                .filter(sc -> sc.getLastApplicationDate() != null && sc.getLastApplicationDate().isAfter(threshold))
                .toList();

        boolean isValid = !controls.isEmpty();
        logger.debug("Evaluando control sanitario [{}] para animal {} → {}", config.getType(), animal.getId(), isValid ? "al día" : "atrasado");
        return !isValid;
    }

    private void generateNotification(CommunityAnimal animal, MunicipalPreventiveCareConfiguration config) {
        String today = LocalDate.now().toString();
        int monthsInterval = config.getValue();
        NotificationTypeEnum typeEnum = NotificationTypeEnum.SANITARY_ALERT;

        List<Notification> previousNotifs = notificationRepository.findByUserIdAndNotificationType_Name(
                animal.getUser().getId(), typeEnum.getName());

        Optional<Notification> latestMatchingTypeNotif = previousNotifs.stream()
                .filter(n -> n.getTitle() != null && n.getTitle().contains(config.getType().getName()))
                .max(Comparator.comparing(Notification::getDateIssued));

        if (latestMatchingTypeNotif.isPresent()) {
            LocalDate lastSentDate = LocalDate.parse(latestMatchingTypeNotif.get().getDateIssued());
            if (lastSentDate.plusMonths(monthsInterval).isAfter(LocalDate.now())) {
                logger.info("Notificación omitida para tipo {}. Última fue en {} y el intervalo aún no se cumple ({} meses)",
                        config.getType().getName(), lastSentDate, monthsInterval);
                return;
            }
        }

        NotificationType typeEntity = notificationTypeRepository.findByName(typeEnum.getName())
                .orElseThrow(() -> new IllegalStateException("Tipo SANITARY_ALERT no encontrado"));

        NotificationStatus statusEntity = notificationStatusRepository.findByName(NotificationStatusEnum.SENT.getName())
                .orElseThrow(() -> new IllegalStateException("Estado SENT no encontrado"));

        Notification notif = Notification.builder()
                .user(animal.getUser())
                .title("Recordatorio de control sanitario: " + config.getType().getName())
                .description("Tu animal registrado requiere atención para el tipo: " + config.getType().getName() +
                        ". Por favor realiza el control correspondiente.")
                .dateIssued(today)
                .notificationStatus(statusEntity)
                .notificationType(typeEntity)
                .build();

        notificationRepository.save(notif);
        logger.info("Notificación generada para usuario {} por tipo {}", animal.getUser().getId(), config.getType().getName());
    }
}
