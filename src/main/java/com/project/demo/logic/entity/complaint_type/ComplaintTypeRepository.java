package com.project.demo.logic.entity.complaint_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing ComplaintType entities.
 * This interface extends JpaRepository to provide CRUD operations.
 * @author dgutierrez
 */
public interface ComplaintTypeRepository extends JpaRepository<ComplaintType, Long> {
    /**
     * Finds a ComplaintType by its name.
     *
     * @param name the name of the complaint type
     * @return an Optional containing the ComplaintType if found, or empty if not found
     */
    Optional<ComplaintType> findByName(String name);
}
