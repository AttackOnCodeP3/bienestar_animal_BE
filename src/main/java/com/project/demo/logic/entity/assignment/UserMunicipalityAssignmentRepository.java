package com.project.demo.logic.entity.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMunicipalityAssignmentRepository extends JpaRepository<UserMunicipalityAssignment, Long> {
    Optional<UserMunicipalityAssignment> findByUserId(Long userId);
}
