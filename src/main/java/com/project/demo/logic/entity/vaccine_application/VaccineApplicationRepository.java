package com.project.demo.logic.entity.vaccine_application;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing VaccineApplication entities.
 * Provides methods to perform CRUD operations on VaccineApplication data.
 *
 * @author dgutierrez
 */
public interface VaccineApplicationRepository extends JpaRepository<VaccineApplication, Long> {
}
