package com.project.demo.logic.entity.sex;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing Sex entities.
 * Provides methods for performing CRUD operations and finding entities by name.
 * @author dgutierrez
 */
public interface SexRepository extends JpaRepository<Sex, Long> {
    Optional<Sex> findByName(String name);
}
