package com.project.demo.logic.entity.user;

import com.project.demo.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private EmailService emailService;

    public boolean resetPasswordAndSendEmail(String email) throws IOException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String tempPassword = generateTemporaryPassword();
            //user.setTemporaryPassword(passwordEncoder.encode(tempPassword));
            user.setTemporaryPassword(tempPassword);
            user.setRequiresPasswordChange(true);
            user.setPassword(passwordEncoder.encode(tempPassword));

            userRepository.save(user);
            emailService.sendEmail(email, "Contraseña Temporal - Bienestar Animal",
                    "Tu contraseña temporal es: " + tempPassword);
            return true;
        }
        return false;
    }

    private String generateTemporaryPassword() {

        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public boolean passwordReset(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }
            if (!newPassword.equals(confirmPassword)) {
                return false;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setRequiresPasswordChange(false);
            user.setTemporaryPassword(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }



}