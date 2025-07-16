package com.project.demo.logic.entity.species;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @author dgutierrez
 */
public interface SpeciesRepository extends JpaRepository<Species, Long> {
    /**
     * Finds a Species by its name.
     *
     * @param name the name of the species
     * @return an Optional containing the Species if found, or empty if not found
     */
    Optional<Species> findByName(String name);

    @Query("SELECT s FROM Species s LEFT JOIN FETCH s.vaccines WHERE s.name = :name")
    Optional<Species> findByNameWithVaccines(@Param("name") String name);
}
