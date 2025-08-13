package com.project.demo.logic.entity.diagnostico_ia;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticoIaRepository extends JpaRepository<DiagnosticoIa, Long> {
Page<DiagnosticoIa> findByAnimalId(Long animalId, Pageable pageable);

    List<DiagnosticoIa> findByAnimalId(Long animalId);
}
