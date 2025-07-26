package com.project.demo.logic.entity.community_animal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
