package com.project.demo.rest.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDTO {
    private Long userId;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}