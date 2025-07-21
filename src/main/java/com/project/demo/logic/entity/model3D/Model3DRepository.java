package com.project.demo.logic.entity.model3D;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing Model3D entities.
 * This interface extends JpaRepository to provide CRUD operations and custom query methods.
 * @author nav
 */
@Repository
public interface Model3DRepository extends JpaRepository<Model3D, Long> {

    Optional<Model3D> findByAnimalId(Long animalId);

    boolean existsByAnimalId(Long animalId);
}
