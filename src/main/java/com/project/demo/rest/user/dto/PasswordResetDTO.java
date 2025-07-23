package com.project.demo.rest.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for handling password reset requests.
 * Contains the necessary fields to validate and process a password change for a user.
 * 
 * <p>
 * Fields include the user's ID, current password, new password, and confirmation of the new password.
 * </p>
 * 
 * @author @aBlancoC
 */
@Getter
@Setter
public class PasswordResetDTO {
    private Long userId;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}