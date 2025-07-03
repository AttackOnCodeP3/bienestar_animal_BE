package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Order(2)
@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;


    private final Logger logger = Logger.getLogger(RoleSeeder.class.getName());

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {

        if (roleRepository.count() > 0) {
            logger.info("Roles already exist, skipping seeding.");
            return;
        }

        RoleEnum[] roleNames = new RoleEnum[] { RoleEnum.COMMUNITY_USER, RoleEnum.VOLUNTEER_USER, RoleEnum.MUNICIPAL_ADMIN, RoleEnum.SUPER_ADMIN };
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.COMMUNITY_USER, "Community user role",
                RoleEnum.VOLUNTEER_USER, "Volunteer user role",
                RoleEnum.MUNICIPAL_ADMIN, "Municipal admin role",
                RoleEnum.SUPER_ADMIN, "Super admin role"
        );

        Arrays.stream(roleNames).forEach((roleName) -> {
            Optional<Role> optionalRole = roleRepository.findByName(roleName);

            optionalRole.ifPresentOrElse(System.out::println, () -> {
                Role roleToCreate = new Role();

                roleToCreate.setName(roleName);
                roleToCreate.setDescription(roleDescriptionMap.get(roleName));

                roleRepository.save(roleToCreate);
            });
        });

        logger.info("Roles have been loaded successfully.");
    }
}
