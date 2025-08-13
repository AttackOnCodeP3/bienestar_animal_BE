package com.project.demo.logic.entity.notification_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing NotificationType entities.
 * Provides methods to perform CRUD operations and custom queries.
 *
 * @author dgutierrez
 */
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
    Optional<NotificationType> findByName(String name);
}
