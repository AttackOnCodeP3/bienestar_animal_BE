package com.project.demo.logic.seeds;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(GeneralConstants.ADMIN_SEEDER_ORDER)
@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MunicipalityRepository municipalityRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    private final PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MunicipalityRepository municipalityRepository,
            NeighborhoodRepository neighborhoodRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.municipalityRepository = municipalityRepository;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        createSuperAdmin();
        createMunicipalAdmin();
        createVolunteerUser();
        createCommunityUser();
        createCensistaUser();
    }

    private void createSuperAdmin() {
        final String email = "super.admin@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Super admin already exists, skipping creation.");
            return;
        }

        List<Role> allRoles = new ArrayList<>();
        roleRepository.findAll().forEach(allRoles::add);

        if (allRoles.isEmpty()) {
            logger.error("No roles found in the database.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.error("Municipality or neighborhood not found.");
            return;
        }

        User user = new User();
        user.setName("Super");
        user.setLastname("Admin");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123"));
        user.setPhoneNumber("88888888");
        user.setBirthDate(LocalDate.of(1980, 1, 1));
        user.setIdentificationCard("999999999");
        user.setMunicipality(municipality.get());
        user.setNeighborhood(neighborhood.get());

        allRoles.forEach(user::addRole);

        userRepository.save(user);
        logger.info("SUPER_ADMIN user created with all roles and email: " + email);
    }

    private void createMunicipalAdmin() {
        final String email = "municipal.admin@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Municipal admin already exists, skipping creation.");
            return;
        }

        Optional<Role> role = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN);
        if (role.isEmpty()) {
            logger.error("MUNICIPAL_ADMIN role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.error("Municipality or neighborhood not found.");
            return;
        }

        User user = new User();
        user.setName("Municipal");
        user.setLastname("Admin");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123"));
        user.setPhoneNumber("87777777");
        user.setBirthDate(LocalDate.of(1985, 5, 10));
        user.setIdentificationCard("888888888");
        user.setMunicipality(municipality.get());
        user.setNeighborhood(neighborhood.get());
        user.addRole(role.get());

        userRepository.save(user);
        logger.info("MUNICIPAL_ADMIN user created with email: " + email);
    }

    private void createVolunteerUser() {
        final String email = "volunteer.user@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Volunteer user already exists, skipping creation.");
            return;
        }

        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);
        Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);

        if (volunteerRole.isEmpty() || communityRole.isEmpty()) {
            logger.error("VOLUNTEER_USER or COMMUNITY_USER role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.error("Municipality or neighborhood not found.");
            return;
        }

        User user = new User();
        user.setName("Volunteer");
        user.setLastname("Demo");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123"));
        user.setPhoneNumber("86666666");
        user.setBirthDate(LocalDate.of(1992, 3, 15));
        user.setIdentificationCard("777777777");
        user.setMunicipality(municipality.get());
        user.setNeighborhood(neighborhood.get());

        user.addRole(volunteerRole.get());
        user.addRole(communityRole.get());

        userRepository.save(user);
        logger.info("VOLUNTEER_USER with COMMUNITY_USER role created with email: " + email);
    }

    private void createCommunityUser() {
        final String email = "community.user@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Community user already exists, skipping creation.");
            return;
        }

        Optional<Role> role = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
        if (role.isEmpty()) {
            logger.error("COMMUNITY_USER role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.error("Municipality or neighborhood not found.");
            return;
        }

        User user = new User();
        user.setName("Community");
        user.setLastname("User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123"));
        user.setPhoneNumber("85555555");
        user.setBirthDate(LocalDate.of(1995, 6, 20));
        user.setIdentificationCard("666666666");
        user.setMunicipality(municipality.get());
        user.setNeighborhood(neighborhood.get());
        user.addRole(role.get());

        userRepository.save(user);
        logger.info("COMMUNITY_USER created with email: " + email);
    }

    private void createCensistaUser() {
        final String email = "censista.user@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Censista user already exists, skipping creation.");
            return;
        }

        Optional<Role> censistaRole = roleRepository.findByName(RoleEnum.CENSISTA_USER);
        if (censistaRole.isEmpty()) {
            logger.warn("CENSISTA_USER role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.warn("Municipality or neighborhood not found.");
            return;
        }

        User user = new User();
        user.setName("Censista");
        user.setLastname("Demo");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123"));
        user.setPhoneNumber("84444444");
        user.setBirthDate(LocalDate.of(1990, 7, 10));
        user.setIdentificationCard("555555555");
        user.setMunicipality(municipality.get());
        user.setNeighborhood(neighborhood.get());
        user.addRole(censistaRole.get());

        userRepository.save(user);
        logger.info("CENSISTA_USER created with email: " + email);
    }
}
