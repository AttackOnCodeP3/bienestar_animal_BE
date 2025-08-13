package com.project.demo.logic.entity.complaint;
import java.util.List;

import com.project.demo.logic.entity.complaint_type.ComplaintType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing Complaint entities.
 * This interface extends JpaRepository to provide CRUD operations.
 *
 * @author dgutierrez
 */
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
        @Query("SELECT COUNT(c) FROM Complaint c WHERE c.complaintState.name = 'Abierta'")
        long countOpenComplaints();

        @Query("SELECT c.complaintType.name, COUNT(c) FROM Complaint c GROUP BY c.complaintType.name")
        List<Object[]> countComplaintsByType();
    /**
     * Finds a Complaint by its ID and fetches it eagerly with its associated entities.
     *
     * @param municipalityId the ID of the municipality to which the complaint belongs
     *                       (used to filter complaints)
     * @param typeId        the ID of the complaint type (optional filter)
     * @param stateId       the ID of the complaint state (optional filter)
     * @return an Optional containing the Complaint if found, or empty if not found
     * @author dgutierrez
     */
    @Query("""
            SELECT c FROM Complaint c
            WHERE c.createdBy.municipality.id = :municipalityId
            AND (:typeId IS NULL OR c.complaintType.id = :typeId)
            AND (:stateId IS NULL OR c.complaintState.id = :stateId)
            """)
    Page<Complaint> findByMunicipalityAndOptionalFilters(
            @Param("municipalityId") Long municipalityId,
            @Param("typeId") Long typeId,
            @Param("stateId") Long stateId,
            Pageable pageable
    );

    /**
     * Finds a Complaint by its ID and fetches it eagerly with its associated entities.
     *
     * @param userId the ID of the user who created the complaint
     *               (used to filter complaints)
     * @param typeId the ID of the complaint type (optional filter)
     * @param stateId the ID of the complaint state (optional filter)
     * @return a Page containing Complaints created by the specified user, filtered by optional type and state
     * @author dgutierrez
     */
    @Query("""
    SELECT c FROM Complaint c
    WHERE c.createdBy.id = :userId
    AND (:typeId IS NULL OR c.complaintType.id = :typeId)
    AND (:stateId IS NULL OR c.complaintState.id = :stateId)
""")
    Page<Complaint> findByUserAndOptionalFilters(
            @Param("userId") Long userId,
            @Param("typeId") Long typeId,
            @Param("stateId") Long stateId,
            Pageable pageable
    );
}
