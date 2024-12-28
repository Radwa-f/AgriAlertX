package com.example.demo.utils.email;


import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Primary  // Mark this as the primary bean to be injected
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender javaMailSender;

    public EmailSenderImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(String to, String emailContent) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);  // true indicates multipart (for HTML emails)
            helper.setFrom("douidysifeddine@gmail.com");  // Set from email address
            helper.setTo(to);  // Set the recipient email address
            helper.setSubject("AgriAlert Registration");  // Subject of the email
            helper.setText(emailContent, true);  // Set email content (HTML)

            javaMailSender.send(mimeMessage);  // Send the email
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);  // Handle failure
        }
    }
}
