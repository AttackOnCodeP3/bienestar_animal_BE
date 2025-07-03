package com.project.demo.logic.seeds;

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
import java.util.Optional;
import java.util.logging.Logger;

@Order(5)
@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MunicipalityRepository municipalityRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    private final PasswordEncoder passwordEncoder;

    private final Logger logger = Logger.getLogger(AdminSeeder.class.getName());


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository  userRepository,
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
    }

    private void createSuperAdmin() {
        final String email = "super.admin@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Super admin already exists, skipping creation.");
            return;
        }

        Optional<Role> role = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (role.isEmpty()) {
            logger.warning("SUPER_ADMIN role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.warning("Municipality or neighborhood not found.");
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
        user.addRole(role.get());

        userRepository.save(user);
        logger.info("SUPER_ADMIN user created with email: " + email);
    }

    private void createMunicipalAdmin() {
        final String email = "municipal.admin@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Municipal admin already exists, skipping creation.");
            return;
        }

        Optional<Role> role = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN);
        if (role.isEmpty()) {
            logger.warning("MUNICIPAL_ADMIN role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.warning("Municipality or neighborhood not found.");
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
            logger.warning("VOLUNTEER_USER or COMMUNITY_USER role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.warning("Municipality or neighborhood not found.");
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
            logger.warning("COMMUNITY_USER role not found.");
            return;
        }

        Optional<Municipality> municipality = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(1L);

        if (municipality.isEmpty() || neighborhood.isEmpty()) {
            logger.warning("Municipality or neighborhood not found.");
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
}
