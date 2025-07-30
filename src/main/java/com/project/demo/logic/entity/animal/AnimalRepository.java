package com.project.demo.logic.entity.animal;


import com.project.demo.rest.animal.dto.AnimalRecordDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Animal entities.
 * @author dgutierrez
 * @updatedBy gjimenez
 */
public interface AnimalRepository extends JpaRepository<Animal, Long> {
   // Optional<AnimalRecordDTO> findByIdAndUserId(Long animalId, Long userId);

    @Query("SELECT a FROM Animal a JOIN CommunityAnimal c ON a.id = c.id WHERE c.user.id = :userId")
    List<Animal> findCommunityAnimalsByUserId(@Param("userId") Long userId);

}
