package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.logic.entity.complaint.ComplaintRepository;
import com.project.demo.logic.entity.complaint_state.ComplaintState;
import com.project.demo.logic.entity.complaint_state.ComplaintStateEnum;
import com.project.demo.logic.entity.complaint_state.ComplaintStateRepository;
import com.project.demo.logic.entity.complaint_type.ComplaintType;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(GeneralConstants.COMPLAINT_SEEDER_ORDER)
@Component
@AllArgsConstructor
public class ComplaintSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintSeeder.class);

    private final ComplaintRepository complaintRepository;
    private final ComplaintTypeRepository complaintTypeRepository;
    private final ComplaintStateRepository complaintStateRepository;
    private final MunicipalityRepository municipalityRepository;
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedComplaints();
    }

    private void seedComplaints() {
        if (complaintRepository.count() > 0) {
            logger.info("Complaints already exist, skipping seeding.");
            return;
        }

        Optional<Municipality> optionalMunicipality =
                municipalityRepository.findByName(GeneralConstants.NAME_OF_MUNICIPALITY_LA_UNION_CARTAGO_SEEDER);

        Optional<ComplaintType> optionalType =
                complaintTypeRepository.findAll().stream().findFirst();

        Optional<ComplaintState> optionalState =
                complaintStateRepository.findByName(ComplaintStateEnum.OPEN.getName());

        Optional<User> optionalUser =
                userRepository.findAll().stream()
                        .filter(u -> u.getMunicipality() != null &&
                                u.getMunicipality().getName().equals(GeneralConstants.NAME_OF_MUNICIPALITY_LA_UNION_CARTAGO_SEEDER))
                        .findFirst();

        if (optionalMunicipality.isEmpty() || optionalType.isEmpty() || optionalState.isEmpty() || optionalUser.isEmpty()) {
            logger.warn("Missing required entities to seed complaint. Skipping.");
            return;
        }

        Complaint complaint = Complaint.builder()
                .description("Perro abandonado en el parque.")
                .latitude(9.903716)
                .longitude(-83.995588)
                .imageUrl("https://placehold.co/600x400?text=Complaint")
                .complaintType(optionalType.get())
                .complaintState(optionalState.get())
                .createdBy(optionalUser.get())
                .build();

        complaintRepository.save(complaint);
        logger.info("Seeded 1 complaint in state '{}' for municipality '{}'.",
                optionalState.get().getName(),
                optionalMunicipality.get().getName());
    }
}
