package com.project.demo.rest.user;

import com.project.demo.logic.entity.user.ForgotPasswordService;
import com.project.demo.rest.user.dto.ForgotPasswordDTO;
import com.project.demo.rest.user.dto.PasswordResetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for handling user password-related operations such as 
 * requesting a password reset and updating the password.
 *
 * @author @aBlancoC
 */
@RestController
@RequestMapping("/users")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    /**
     * Handles the forgot password request by sending a temporary password 
     * to the user's email if the email is registered.
     *
     * @param request The request containing the user's email.
     * @return A ResponseEntity with a success or error message.
     * @throws IOException If an error occurs while sending the email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO request) throws IOException {
        try {
            boolean sent = forgotPasswordService.resetPasswordAndSendEmail(request.getEmail());
            if (sent) {
                return ResponseEntity.ok().body(Map.of(
                        "message", "Si existe un usuario registrado con ese correo electrónico, se ha enviado una contraseña temporal a su dirección de correo."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Si se pudo enviar el correo de restablecimiento de contraseña."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Ocurrió un error al procesar la solicitud."
            ));
        }
    }

    /**
     * Handles the password reset request by updating the user's password 
     * if the provided credentials are valid.
     *
     * @param request The request containing user ID, current password, 
     *                new password, and confirmation of the new password.
     * @return A ResponseEntity with a success or error message.
     */
    @PostMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody PasswordResetDTO request) {
        boolean reset = forgotPasswordService.passwordReset(
                request.getUserId(),
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        if (reset) {
            return ResponseEntity.ok().body(Map.of("message", "Contraseña actualizada correctamente."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Error al actualizar la contraseña. Verifique sus credenciales."));
        }
    }
}
