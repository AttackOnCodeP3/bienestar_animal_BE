package com.project.demo.logic.entity.sex;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing
 * @author dgutierrez
 */
public interface SexRepository extends JpaRepository<Sex, Long> {
    Optional<Sex> findByName(String name);
}
