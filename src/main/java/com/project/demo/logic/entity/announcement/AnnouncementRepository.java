package com.project.demo.logic.entity.announcement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing Announcement entities.
 * @author dgutierrez
 */
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    /**
     * Finds an Announcement entity by its title.
     * @param title the title of the announcement to search for.
     * @return an Optional containing the Announcement if found, or empty if not found.
     */
    Optional<Announcement> findByTitle(String title);

    /**
     * Finds all Announcement entities by their state ID.
     * @param stateId the ID of the announcement state to filter by.
     * @param pageable the pagination information.
     * @return a Page containing Announcement entities that match the given state ID.
     */
    Page<Announcement> findByState_Id(Long stateId, Pageable pageable);

    /**
     * Finds all Announcement entities by their municipality ID.
     * @param id the ID of the municipality to filter by.
     * @param pageable the pagination information.
     * @return a Page containing Announcement entities that match the given municipality ID.
     */
    Page<Announcement> findByMunicipalities_Id(Long id, Pageable pageable);

    /**
     * Finds all Announcement entities by their state ID and municipality ID.
     * @param stateId the ID of the announcement state to filter by.
     * @param municipalityId the ID of the municipality to filter by.
     * @param pageable the pagination information.
     * @return a Page containing Announcement entities that match the given state ID and municipality ID.
     */
    Page<Announcement> findByState_IdAndMunicipalities_Id(Long stateId, Long municipalityId, Pageable pageable);

    /**
     * Finds an Announcement entity by its ID and municipality ID.
     * @param id the ID of the announcement to search for.
     * @param municipalityId the ID of the municipality to filter by.
     * @return an Optional containing the Announcement if found, or empty if not found.
     */
    Optional<Announcement> findByIdAndMunicipalities_Id(Long id, Long municipalityId);
}
