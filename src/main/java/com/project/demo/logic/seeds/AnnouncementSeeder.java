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
                .title("Evento Adiestramiento canino")
                .description("""
        <p>Únete a nuestra <strong>jornada especial de adiestramiento canino</strong> en la playa, 
        donde expertos en comportamiento animal compartirán <em>técnicas prácticas</em> para mejorar 
        la obediencia y la convivencia con tu mascota. 
        </p>
        <ul>
            <li>Entrenamiento básico y avanzado</li>
            <li>Consejos para socialización entre perros</li>
            <li>Actividades recreativas y juegos guiados</li>
        </ul>
        <p><strong>¡No olvides llevar agua, correa y mucha energía!</strong></p>
    """)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .imageUrl("https://i.imgur.com/oJj0Y69.jpeg")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        Announcement announcement2 = Announcement.builder()
                .title("Evento cuidados de los felinos")
                .description("""
        <p>Descubre cómo <strong>mejorar la calidad de vida de tus gatos</strong> con\s
        recomendaciones de especialistas en salud y comportamiento felino.</p>
        <ul>
            <li>Alimentación balanceada para cada etapa de vida</li>
            <li>Enriquecimiento ambiental y juegos</li>
            <li>Prevención de enfermedades comunes</li>
        </ul>
        <p>Ideal para dueños primerizos y amantes de los felinos.</p>
   \s""")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .imageUrl("https://i.imgur.com/Fv45TzL.jpeg")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        Announcement announcement3 = Announcement.builder()
                .title("Capacitación Alimentación saludable")
                .description("""
        <p>Aprende con profesionales de la nutrición animal sobre\s
        <strong>dietas equilibradas y saludables</strong> para perros y gatos.</p>
        <ul>
            <li>Elección de alimentos según edad y condición física</li>
            <li>Snacks naturales y caseros</li>
            <li>Hábitos que mejoran la digestión y el bienestar</li>
        </ul>
        <p>Incluye material digital y asesoría personalizada.</p>
   \s""")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .imageUrl("https://i.imgur.com/3MQCUBZ.jpeg")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        Announcement announcement4 = Announcement.builder()
                .title("Campaña de castración")
                .description("""
        <p>Participa en nuestra <strong>campaña masiva de castración</strong> y ayuda\s
        a controlar la sobrepoblación animal en nuestra comunidad.</p>
        <ul>
            <li>Procedimientos realizados por veterinarios certificados</li>
            <li>Recuperación supervisada y cuidados postoperatorios</li>
            <li>Precios accesibles y cupos limitados</li>
        </ul>
        <p><em>¡Tu compromiso salva vidas!</em></p>
   \s""")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .imageUrl("https://i.imgur.com/COiI5zt.jpeg")
                .state(state)
                .municipalities(List.of(municipality))
                .build();

        announcementRepository.saveAll(List.of(announcement1, announcement2, announcement3, announcement4));

        logger.info("Seeded 2 announcements in state '{}' for municipality '{}'.",
                state.getName(), municipality.getName());
    }
}
