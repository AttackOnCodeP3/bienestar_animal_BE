package com.project.demo.rest.user;

import com.project.demo.logic.entity.user.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        boolean sent = forgotPasswordService.resetPasswordAndSendEmail(email);
        if (sent) {
            return ResponseEntity.ok("A temporary password has been sent to your email.");
        } else {
            return ResponseEntity.badRequest().body("User with that email does not exist.");
        }
    }
}