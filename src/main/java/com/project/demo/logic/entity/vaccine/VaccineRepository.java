package com.project.demo.logic.entity.vaccine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for managing Vaccine entities.
 * <p>
 * Provides methods to perform CRUD operations and custom queries on Vaccine entities.
 * </p>
 *
 * @author dgutierrez
 */
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    @Query("SELECT v FROM Vaccine v JOIN v.species s WHERE s.id = :speciesId")
    Page<Vaccine> findAllBySpeciesId(@Param("speciesId") Long speciesId, Pageable pageable);

    Optional<Vaccine> findByName(String name);
}
