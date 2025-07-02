package com.project.demo.logic.entity.canton;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CantonRepository extends JpaRepository<Canton,Long> {
    Optional<Canton> findByName(String name);
}
