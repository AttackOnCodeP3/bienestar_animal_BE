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

    /**
     * Retrieves a list of community animals associated with a specific user.
     *
     * @param userId the ID of the user whose community animals are to be found
     * @return a list of {@link Animal} entities linked to the given user
     * @author aBlancoC
     */
    @Query("SELECT a FROM Animal a JOIN CommunityAnimal c ON a.id = c.id WHERE c.user.id = :userId")
    List<Animal> findCommunityAnimalsByUserId(@Param("userId") Long userId);



    @Query("SELECT a FROM AbandonedAnimal a WHERE a.createdBy.id = :userId")
    List<AbandonedAnimal> findAbandonedAnimalsByUserId(@Param("userId") Long userId);


}
