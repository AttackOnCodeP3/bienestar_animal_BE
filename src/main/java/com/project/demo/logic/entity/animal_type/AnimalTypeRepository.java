package com.project.demo.logic.entity.animal_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author dgutierrez
 */
public interface AnimalTypeRepository extends JpaRepository<AnimalType, Long> {
    /**
     * Finds an AnimalType by its name.
     *
     * @param name the name of the animal type
     * @return an Optional containing the AnimalType if found, or empty if not found
     */
    Optional<AnimalType> findByName(String name);
}