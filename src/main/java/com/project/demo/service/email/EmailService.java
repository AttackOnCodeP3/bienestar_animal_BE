package com.project.demo.service.email;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // Implement actual email sending here using JavaMailSender or similar
        // For now, just print to console for demonstration
        System.out.println("Sending email to " + to + " with subject: " + subject);
        System.out.println("Body: " + body);
    }
}