package com.project.demo.logic.entity.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    /**
     * Checks if a notification exists for a user by their ID, notification type name, and date issued.
     *
     * @param userId              the ID of the user
     * @param notificationTypeName the name of the notification type
     * @param dateIssued          the date when the notification was issued
     * @return true if a notification exists, false otherwise
     * @author dgutierrez
     */
    boolean existsByUserIdAndNotificationType_NameAndDateIssued(Long userId, String notificationTypeName, String dateIssued);

    @Query("""
    SELECT n FROM Notification n
    WHERE n.user.id = :userId
      AND n.notificationType.name = :notificationTypeName
    ORDER BY n.createdAt DESC
    LIMIT 1
""")
    Optional<Notification> findLatestByUserAndType(
            @Param("userId") Long userId,
            @Param("notificationTypeName") String typeName
    );

    List<Notification> findByUserIdAndNotificationType_Name(Long userId, String notificationTypeName);

    @Transactional
    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.notificationStatus.id = :readStatusId
    WHERE n.user.email = :email
""")
    int markAllAsReadForUser(@Param("email") String email, @Param("readStatusId") Long readStatusId);
}
