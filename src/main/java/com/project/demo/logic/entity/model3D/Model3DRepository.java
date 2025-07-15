package com.project.demo.logic.entity.model3D;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Model3DRepository extends JpaRepository<Model3D, Long> {
    Optional<Model3D> findByAnimalId(Long animalId);
    boolean existsByAnimalId(Long animalId);
}