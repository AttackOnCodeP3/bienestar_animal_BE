package com.project.demo.logic.entity.user;

import com.project.demo.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

/**
 * Service class for handling forgot password functionality.
 *
 * @author @aBlancoC
 */
@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Resets the user's password and sends a temporary password to their email.
     *
     * @param email The email address of the user requesting a password reset.
     * @return true if the user exists and the email was sent, false otherwise.
     * @throws IOException if an error occurs while sending the email.
     */
    public boolean resetPasswordAndSendEmail(String email) throws IOException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String tempPassword = generateTemporaryPassword();
            user.setTemporaryPassword(passwordEncoder.encode(tempPassword));
            user.setRequiresPasswordChange(true);
            user.setPassword(passwordEncoder.encode(tempPassword));

            userRepository.save(user);
            emailService.sendEmail(email, "Contraseña Temporal - Bienestar Animal",
                    "Tu contraseña temporal es: " + tempPassword);
            return true;
        }
        return false;
    }

    /**
     * Generates a random temporary password.
     *
     * @return A randomly generated password string.
     */
    private String generateTemporaryPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    /**
     * Resets the user's password if the current password matches and the new passwords are valid.
     *
     * @param userId          The ID of the user requesting the password reset.
     * @param currentPassword The user's current password.
     * @param newPassword     The new password to set.
     * @param confirmPassword The confirmation of the new password.
     * @return true if the password was successfully reset, false otherwise.
     */
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
