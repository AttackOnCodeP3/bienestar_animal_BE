package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Order(2)
@Component
public class LocationsSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final CantonRepository cantonRepository;

    public LocationsSeeder(CantonRepository cantonRepository) {
        this.cantonRepository = cantonRepository;
    }

    private final Logger logger = Logger.getLogger(LocationsSeeder.class.getName());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadLocations();
    }

    private void loadLocations() {
        this.seedCantons();
    }

    private void seedCantons() {
        if (cantonRepository.count() > 0) {
            logger.info("Cantons already exist, skipping seeding.");
            return;
        }

        List<Canton> listCantons = new ArrayList<>();

        listCantons.add(Canton.builder().id(1L).name("La unión").build());

        for (Canton canton : listCantons) {
            cantonRepository.save(canton);
            logger.info("Canton saved: " + canton.getName());
        }
    }
}
