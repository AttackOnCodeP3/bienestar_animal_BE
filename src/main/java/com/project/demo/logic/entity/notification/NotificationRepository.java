package com.project.demo.logic.entity.notification;

import lombok.Getter;
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

    /**
     * Counts the number of notifications by their status ID.
     *
     * @param statusId the ID of the notification status
     * @return the count of notifications with the specified status
     * @author dgutierrez
     */
    long countByNotificationStatus_Id(Long statusId);

    /**
     * Counts the number of notifications for a user by their email and status ID.
     *
     * @param email    the email of the user
     * @param statusId the ID of the notification status
     * @return the count of notifications for the specified user and status
     * @author dgutierrez
     */
    long countByUser_EmailAndNotificationStatus_Id(String email, Long statusId);
}
