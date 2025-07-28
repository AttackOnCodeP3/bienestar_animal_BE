package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.announcement.Announcement;
import com.project.demo.logic.entity.announcement.AnnouncementRepository;
import com.project.demo.logic.entity.announcement_state.AnnouncementState;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateEnum;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Order(GeneralConstants.ANNOUNCEMENT_SEEDER_ORDER)
@Component
public class AnnouncementSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementSeeder.class);

    private final AnnouncementRepository announcementRepository;
    private final MunicipalityRepository municipalityRepository;
    private final AnnouncementStateRepository announcementStateRepository;

    public AnnouncementSeeder(
            AnnouncementRepository announcementRepository,
            MunicipalityRepository municipalityRepository,
            AnnouncementStateRepository announcementStateRepository
    ) {
        this.announcementRepository = announcementRepository;
        this.municipalityRepository = municipalityRepository;
        this.announcementStateRepository = announcementStateRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedAnnouncements();
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) {
            logger.info("Announcements already exist, skipping seeding.");
            return;
        }

        Optional<Municipality> optionalMunicipality =
                municipalityRepository.findByName(GeneralConstants.NAME_OF_MUNICIPALITY_LA_UNION_CARTAGO_SEEDER);

        Optional<AnnouncementState> optionalState =
                announcementStateRepository.findByName(AnnouncementStateEnum.PUBLISHED.getName());

        if (optionalMunicipality.isEmpty()) {
            logger.warn("Municipality '{}' not found, skipping Announcement seeding.",
                    GeneralConstants.NAME_OF_MUNICIPALITY_LA_UNION_CARTAGO_SEEDER);
            return;
        }

        if (optionalState.isEmpty()) {
            logger.warn("AnnouncementState '{}' not found, cannot seed announcements.",
                    AnnouncementStateEnum.PUBLISHED.getName());
            return;
        }

        Municipality municipality = optionalMunicipality.get();
        AnnouncementState state = optionalState.get();

        Announcement announcement1 = Announcement.builder()
                .title("Campaña de vacunación animal")
                .description("Vacunación gratuita para perros y gatos el próximo sábado en el parque central.")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .imageUrl("https://placehold.co/600x400?text=Vacunation")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        Announcement announcement2 = Announcement.builder()
                .title("Jornada de adopción")
                .description("Adopta una mascota este domingo en el centro comunitario.")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(12))
                .imageUrl("https://placehold.co/600x400?text=Adopcion")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        announcementRepository.saveAll(List.of(announcement1, announcement2));

        logger.info("Seeded 2 announcements in state '{}' for municipality '{}'.",
                state.getName(), municipality.getName());
    }
}
