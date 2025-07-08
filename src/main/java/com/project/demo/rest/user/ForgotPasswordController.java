package com.project.demo.rest.user;

import com.project.demo.logic.entity.user.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) throws IOException {
        boolean sent = forgotPasswordService.resetPasswordAndSendEmail(email);
        if (sent) {
            return ResponseEntity.ok("Si existe un usuario con ese correo, se ha enviado una contraseña temporal a su correo.");
        } else {
            return ResponseEntity.ok("NO existe un usuario con ese correo, se ha enviado una contraseña temporal a su correo.");
        }
    }
}