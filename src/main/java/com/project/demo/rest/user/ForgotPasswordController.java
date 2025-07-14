package com.project.demo.rest.user;

import com.project.demo.logic.entity.user.ForgotPasswordService;
import com.project.demo.rest.user.dto.ForgotPasswordDTO;
import com.project.demo.rest.user.dto.PasswordResetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController

@RequestMapping("/users")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO request) throws IOException {
        boolean sent = forgotPasswordService.resetPasswordAndSendEmail(request.getEmail());

        return ResponseEntity.ok().body(Map.of(
                "message", "Si existe un usuario registrado con ese correo electrónico, se ha enviado una contraseña temporal a su dirección de correo."
        ));
    }


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