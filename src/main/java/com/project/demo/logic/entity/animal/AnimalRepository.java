package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dgutierrez
 */
public interface AnimalRepository extends JpaRepository<Animal, Long> {
}
