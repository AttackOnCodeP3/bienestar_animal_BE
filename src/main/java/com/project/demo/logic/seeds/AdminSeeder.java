package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
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

    private final PasswordEncoder passwordEncoder;

    private final Logger logger = Logger.getLogger(AdminSeeder.class.getName());


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository  userRepository,
            PasswordEncoder passwordEncoder,
            MunicipalityRepository municipalityRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.municipalityRepository = municipalityRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        if (userRepository.count() > 0) {
            logger.info("Super Administrator already exists, skipping seeding.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setName("Super");
        superAdmin.setLastname("Admin");
        superAdmin.setEmail("super.admin@gmail.com");
        superAdmin.setPassword("superadmin123");
        superAdmin.setPhoneNumber("1234567890");
        superAdmin.setBirthDate(LocalDate.of(1990, 1, 1));
        superAdmin.setIdentificationCard("123456789");

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        Optional<User> optionalUser = userRepository.findByEmail(superAdmin.getEmail());
        Optional<Municipality> optionalMunicipality = municipalityRepository.findById(1L);

        if (optionalRole.isEmpty() || optionalUser.isPresent() || optionalMunicipality.isEmpty()) {
            return;
        }

        var user = new User();
        user.setName(superAdmin.getName());
        user.setLastname(superAdmin.getLastname());
        user.setEmail(superAdmin.getEmail());
        user.setBirthDate(superAdmin.getBirthDate());
        user.setPhoneNumber(superAdmin.getPhoneNumber());
        user.setIdentificationCard(superAdmin.getIdentificationCard());
        user.setMunicipality(optionalMunicipality.get());
        user.setPassword(passwordEncoder.encode(superAdmin.getPassword()));
        user.setRole(optionalRole.get());

        userRepository.save(user);

        logger.info("Super Administrator created with email: " + superAdmin.getEmail());
    }
}
