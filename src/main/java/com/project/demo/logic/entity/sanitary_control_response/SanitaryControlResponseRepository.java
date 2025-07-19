package com.project.demo.logic.entity.sanitary_control_response;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing SanitaryControlResponse entities.
 * @author dgutierrez
 */
public interface SanitaryControlResponseRepository extends JpaRepository<SanitaryControlResponse, Long> {
    Optional<SanitaryControlResponse> findByName(String name);
}
