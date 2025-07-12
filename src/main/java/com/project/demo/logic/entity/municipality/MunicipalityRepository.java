package com.project.demo.logic.entity.municipality;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository interface for managing Municipality entities.
 * @author dgutierrez
 */
public interface MunicipalityRepository extends JpaRepository<Municipality, Long>, JpaSpecificationExecutor<Municipality> {
    /**
     *
     * @param cantonId
     * @return
     * @author dgutierrez
     */
    boolean existsByCantonId(Long cantonId);

    /**
     * Finds a Municipality by its email.
     *
     * @param email the email of the municipality
     * @return an Optional containing the Municipality if found, or empty if not found
     * @author dgutierrez
     */
    Optional<Municipality> findByEmail(String email);
}
