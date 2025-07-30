package com.project.demo.logic.entity.community_animal;

import com.project.demo.rest.animal.dto.AnimalRecordDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author dgutierrez
 */
public interface CommunityAnimalRepository extends JpaRepository<CommunityAnimal, Long> {
    /**
     * Finds paginated CommunityAnimals associated with a specific user email.
     *
     * @param email    the email of the user
     * @param pageable the pagination information
     * @return a page of CommunityAnimal entities
     * @author dgutierrez
     */
    Page<CommunityAnimal> findByUser_Email(String email, Pageable pageable);

    /**
     * Checks if a user has registered animals by their email.
     * @param email the email of the user
     * @return true if the user has registered animals
     */
    boolean existsByUser_Email(String email);


   AnimalRecordDTO findByIdAndUserId(Long animalId, Long userId);



}
