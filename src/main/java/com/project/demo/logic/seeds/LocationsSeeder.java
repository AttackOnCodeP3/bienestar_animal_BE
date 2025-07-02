package com.project.demo.logic.seeds;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.district.District;
import com.project.demo.logic.entity.district.DistrictRepository;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

@Order(1)
@Component
public class LocationsSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = Logger.getLogger(LocationsSeeder.class.getName());
    private final CantonRepository cantonRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    public LocationsSeeder(
            CantonRepository cantonRepository,
            DistrictRepository districtRepository,
            NeighborhoodRepository neighborhoodRepository
    ) {
        this.cantonRepository = cantonRepository;
        this.districtRepository = districtRepository;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedCantons();
        seedDistricts();
        seedNeighborhoods();
    }

    private void seedCantons() {
        if (cantonRepository.count() > 0) {
            logger.info("Cantons already exist, skipping...");
            return;
        }
        List<Canton> cantons = List.of(
                Canton.builder().name("La Unión").build(),
                Canton.builder().name("Curridabat").build(),
                Canton.builder().name("Goicoechea").build()
        );
        cantonRepository.saveAll(cantons);
        logger.info("Cantons seeded: La Unión, Curridabat, Goicoechea");
    }

    private void seedDistricts() {
        if (districtRepository.count() > 0) {
            logger.info("Districts already exist, skipping...");
            return;
        }

        Map<String, List<String>> cantonDistrictMap = new HashMap<>();
        cantonDistrictMap.put("La Unión", List.of("Tres Ríos", "San Diego", "San Juan"));
        cantonDistrictMap.put("Curridabat", List.of("Curridabat", "Granadilla", "Sánchez"));
        cantonDistrictMap.put("Goicoechea", List.of("Guadalupe", "San Francisco", "Calle Blancos"));

        List<District> districts = new ArrayList<>();
        cantonDistrictMap.forEach((cantonName, distList) -> {
            var canton = cantonRepository.findByName(cantonName)
                    .orElseThrow(() -> new RuntimeException("Canton not found: " + cantonName));
            distList.forEach(distName -> {
                districts.add(District.builder().name(distName).canton(canton).build());
                logger.info("Added district: " + distName + " to canton: " + cantonName);
            });
        });

        districtRepository.saveAll(districts);
        logger.info("Districts seeded successfully.");
    }

    private void seedNeighborhoods() {
        if (neighborhoodRepository.count() > 0) {
            logger.info("Neighborhoods already exist, skipping...");
            return;
        }

        List<Neighborhood> neighborhoods = new ArrayList<>();

        for (var district : districtRepository.findAll()) {
            String cname = district.getCanton().getName();
            String dname = district.getName();

            List<String> realBarrios;
            if (cname.equals("La Unión") && dname.equals("Tres Ríos")) {
                realBarrios = List.of("La Antigua", "Villas", "La Cruz");
            } else if (cname.equals("La Unión") && dname.equals("San Diego")) {
                realBarrios = List.of("Villas de Florencia", "Eulalia", "Florencio del Castillo");
            } else if (cname.equals("La Unión") && dname.equals("San Juan")) {
                realBarrios = List.of("Araucarias", "Colinas de Montealegre", "Danza del Sol");
            } else if (cname.equals("Curridabat") && dname.equals("Curridabat")) {
                realBarrios = List.of("Ahogados", "Aromático", "Cipreses");
            } else if (cname.equals("Curridabat") && dname.equals("Granadilla")) {
                realBarrios = List.of("Mallorca", "Laguna", "Plaza del Sol");
            } else if (cname.equals("Curridabat") && dname.equals("Sánchez")) {
                realBarrios = List.of("Hogar", "Miramontes", "Santa Cecilia");
            } else {
                realBarrios = List.of(dname + " Centro", dname + " Norte", dname + " Sur");
            }

            realBarrios.forEach(nbName -> {
                neighborhoods.add(Neighborhood.builder().name(nbName).district(district).build());
                logger.info("Added barrio: " + nbName + " in district: " + dname);
            });
        }

        neighborhoodRepository.saveAll(neighborhoods);
        logger.info("Neighborhoods seeded successfully.");
    }
}
