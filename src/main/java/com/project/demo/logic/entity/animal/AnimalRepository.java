package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Animal entities.
 * Provides CRUD operations and custom query methods for Animal entities.
 * @author nav
 */
@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {
    
    /**
     * Finds an animal by its name.
     * @param name the name of the animal
     * @return an Optional containing the Animal if found, or empty if not found
     * @author nav
     */
    Optional<Animal> findByName(String name);
    
    boolean existsByName(String name);
}