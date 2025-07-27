package com.project.demo.logic.entity.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    Optional<Notification> findTopByUserIdAndNotificationType_NameAndTitleOrderByDateIssuedDesc(Long userId, String typeName, String title);

    Optional<Notification> findTopByUserIdAndNotificationType_NameOrderByDateIssuedDesc(Long userId, String typeName);

    @Transactional
    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.notificationStatus.id = :readStatusId
    WHERE n.user.email = :email
""")
    int markAllAsReadForUser(@Param("email") String email, @Param("readStatusId") Long readStatusId);
}
