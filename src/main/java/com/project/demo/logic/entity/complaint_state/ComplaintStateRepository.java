package com.project.demo.logic.entity.complaint_state;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing ComplaintState entities.
 * This interface extends JpaRepository to provide CRUD operations.
 *
 * @author dgutierrez
 */
public interface ComplaintStateRepository extends JpaRepository<ComplaintState, Long> {
    /**
     * Finds a ComplaintState by its name.
     *
     * @param name the name of the complaint state
     * @return an Optional containing the ComplaintState if found, or empty if not found
     */
    Optional<ComplaintState> findByName(String name);
}
