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

    @Query("SELECT COUNT(a) FROM Animal a WHERE a.animalType.name = 'Abandonado'")
    long countAbandonedAnimals();

    @Query("SELECT a.animalType.name, COUNT(a) FROM Animal a GROUP BY a.animalType.name")
    List<Object[]> countAnimalsByType();

    /**
     * Retrieves a list of community animals associated with a specific user.
     *
     * @param userId the ID of the user whose community animals are to be found
     * @return a list of {@link Animal} entities linked to the given user
     * @author aBlancoC
     */
    @Query("SELECT a FROM Animal a JOIN CommunityAnimal c ON a.id = c.id WHERE c.user.id = :userId")
    List<Animal> findCommunityAnimalsByUserId(@Param("userId") Long userId);

}
