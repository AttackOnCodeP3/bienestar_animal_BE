package com.project.demo.logic.entity.notification;

/**
 * Represents a predefined template for a notification type.
 * Includes a title, message body, and optional action URL.
 * @param title     Notification title
 * @param message   Main body or short description
 * @param actionUrl Optional URL for redirection
 * @author dgutierrez
 */
public record NotificationTemplate(
        String title,
        String message,
        String actionUrl
) {}
