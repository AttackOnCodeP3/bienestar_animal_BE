package com.project.demo.logic.entity.animal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AbandonedAnimalRepository extends JpaRepository<AbandonedAnimal, Long> {
}
