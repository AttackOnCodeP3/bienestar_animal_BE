package com.project.demo.logic.entity.sanitary_control_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author dgutierrez
 */
public interface SanitaryControlTypeRepository extends JpaRepository<SanitaryControlType, Long> {
    /**
     * Finds a SanitaryControlType by its name.
     *
     * @param name the name of the sanitary control type
     * @return an Optional containing the SanitaryControlType if found, or empty if not found
     */
    Optional<SanitaryControlType> findByName(String name);
}
