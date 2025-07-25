package com.project.demo.logic.entity.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds paginated notifications by the user's email.
     *
     * @param email    the email of the user
     * @param pageable the pagination object
     * @return a page of notifications for the specified user
     * @author dgutierrez
     */
    Page<Notification> findByUser_Email(String email, Pageable pageable);
}
