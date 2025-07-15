package com.project.demo.logic.entity.municipality;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipalityStatusRepository extends JpaRepository<MunicipalityStatus, Long> {
    Optional<MunicipalityStatus> findByName(String name);
}
