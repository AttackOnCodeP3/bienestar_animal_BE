package com.project.demo.rest.user.dto;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for handling forgot password requests.
 * <p>
 * This class encapsulates the user's email address required to initiate
 * a password reset process.
 * </p>
 *
 * @author @aBlancoC
 */
@Getter
@Setter
public class ForgotPasswordDTO {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
