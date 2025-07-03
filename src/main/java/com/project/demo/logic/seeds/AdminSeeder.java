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
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        String email = "super.admin@gmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Super Administrator already exists, skipping seeding.");
            return;
        }

        Optional<Role> superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        Optional<Role> municipalAdminRole = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN);
        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);
        Optional<Role> communityUserRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);

        if (superAdminRole.isEmpty() || municipalAdminRole.isEmpty() ||
                volunteerRole.isEmpty() || communityUserRole.isEmpty()) {
            logger.warning("One or more required roles not found, skipping super admin creation.");
            return;
        }

        Optional<Municipality> municipalityOpt = municipalityRepository.findById(1L);
        Optional<Neighborhood> neighborhoodOpt = neighborhoodRepository.findById(1L);

        if (municipalityOpt.isEmpty() || neighborhoodOpt.isEmpty()) {
            logger.warning("Municipality or Neighborhood not found, skipping super admin creation.");
            return;
        }

        User user = new User();
        user.setName("Super");
        user.setLastname("Admin");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("superadmin123"));
        user.setPhoneNumber("1234567890");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setIdentificationCard("123456789");
        user.setMunicipality(municipalityOpt.get());
        user.setNeighborhood(neighborhoodOpt.get());

        user.addRole(superAdminRole.get());
        user.addRole(municipalAdminRole.get());
        user.addRole(volunteerRole.get());
        user.addRole(communityUserRole.get());

        userRepository.save(user);
        logger.info("Super Administrator created with email: " + email);
    }
}
