package com.project.demo.logic.entity.animal;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing Animal entities.
 * @author dgutierrez
 * @updatedBy gjimenez
 * @updatedBy @aBlancoC
 */
public interface AnimalRepository extends JpaRepository<Animal, Long> {


    /**
     * Retrieves a list of community animals linked to a specific user.
     * <p>
     * This query joins the {@link Animal} entity with {@code CommunityAnimal} using the animal ID,
     * and filters results by the provided user ID.
     * </p>
     *
     * @param userId The ID of the user whose community animals are to be retrieved.
     * @return A list of {@link Animal} objects owned or registered by the specified user.
     *  @author @aBlancoC
     */
    @Query("SELECT a FROM Animal a JOIN CommunityAnimal c ON a.id = c.id WHERE c.user.id = :userId")
    List<Animal> findCommunityAnimalsByUserId(@Param("userId") Long userId);

    /**
     * Retrieves a list of abandoned animals reported by a specific user.
     * <p>
     * This query selects from the {@link AbandonedAnimal} entity where the
     * {@code createdBy} field matches the provided user ID.
     * </p>
     *
     * @param userId The ID of the user who reported the abandoned animals.
     * @return A list of {@link AbandonedAnimal} entities reported by the specified user.
     *  @author @aBlancoC
     */
    @Query("SELECT a FROM AbandonedAnimal a WHERE a.createdBy.id = :userId")
    List<AbandonedAnimal> findAbandonedAnimalsByUserId(@Param("userId") Long userId);