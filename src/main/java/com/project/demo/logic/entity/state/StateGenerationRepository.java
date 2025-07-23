package com.project.demo.logic.entity.state;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateGenerationRepository extends JpaRepository<StateGeneration, Long> {
    Optional<StateGeneration> findByName(String name);
}