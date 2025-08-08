package com.project.demo.logic.entity.notification_status;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing NotificationStatus entities.
 * Provides methods to perform CRUD operations and custom queries.
 *
 * @author dgutierrez
 */
public interface NotificationStatusRepository extends JpaRepository<NotificationStatus, Long> {
    Optional<NotificationStatus> findByName(String email);
}
