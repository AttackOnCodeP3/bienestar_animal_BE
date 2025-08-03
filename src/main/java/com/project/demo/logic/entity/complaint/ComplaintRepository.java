package com.project.demo.logic.entity.complaint;

import com.project.demo.logic.entity.complaint_type.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing ComplaintType entities.
 * This interface extends JpaRepository to provide CRUD operations.
 * @author dgutierrez
 */
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
}
