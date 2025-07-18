package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing Animal entities.
 * @author dgutierrez
 * @updatedBy gjimenez
 */
public interface AnimalRepository extends JpaRepository<Animal, Long> {
}
