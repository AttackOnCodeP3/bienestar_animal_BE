package com.project.demo.logic.entity.animal;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing Animal entities.
 * @author dgutierrez
 * @updatedBy gjimenez
 */
public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("SELECT a FROM Animal a JOIN CommunityAnimal c ON a.id = c.id WHERE c.user.id = :userId")
    List<Animal> findCommunityAnimalsByUserId(@Param("userId") Long userId);

}
