package com.project.demo.logic.entity.district;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCantonId(Long cantonId);
}
