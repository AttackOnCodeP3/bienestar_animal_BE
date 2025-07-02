package com.project.demo.logic.entity.neighborhood;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
    List<Neighborhood> findByDistrictId(Long districtId);
}
