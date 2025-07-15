package com.project.demo.logic.entity.race;

import com.project.demo.logic.entity.species.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author dgutierrez
 */
public interface RaceRepository extends JpaRepository<Race, Long> {

    /**
     * * @author dgutierrez
     */
    Optional<Race> findByNameAndSpecies(String name, Species species);

    Page<Race> findAllBySpecies(Species species, Pageable pageable);
}
