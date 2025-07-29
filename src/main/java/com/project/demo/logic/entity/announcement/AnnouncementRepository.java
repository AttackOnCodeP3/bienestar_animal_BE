package com.project.demo.logic.entity.announcement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Finds Announcement entities by municipality ID and optional filters for title and state ID.
     * @param municipalityId the ID of the municipality to filter by.
     * @param title the title to filter announcements by (optional).
     * @param stateId the ID of the announcement state to filter by (optional).
     * @param pageable the pagination information.
     * @return a Page containing Announcement entities that match the given filters.
     */
    @Query("""
    SELECT a FROM Announcement a
    JOIN a.municipalities m
    WHERE m.id = :municipalityId
    AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:stateId IS NULL OR a.state.id = :stateId)
""")
    Page<Announcement> findByMunicipalityAndOptionalFilters(
            @Param("municipalityId") Long municipalityId,
            @Param("title") String title,
            @Param("stateId") Long stateId,
            Pageable pageable
    );
}
