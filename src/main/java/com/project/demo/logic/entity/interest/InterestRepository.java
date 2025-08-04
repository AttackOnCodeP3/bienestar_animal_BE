package com.project.demo.logic.entity.interest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing Interest entities.
 * Provides methods to perform CRUD operations and custom queries.
 * @author dgutierrez
 */
public interface InterestRepository extends JpaRepository<Interest, Long> {
    /**
     * Finds an Interest entity by its name.
     * @param description the name of the interest to search for.
     * @return an Optional containing the Interest if found, or empty if not found.
     * @author dgutierrez
     */
    Optional<Interest> findByName(String description);
}
