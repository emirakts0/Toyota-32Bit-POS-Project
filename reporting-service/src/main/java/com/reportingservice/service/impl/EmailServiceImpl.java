package com.reportingservice.service.impl;

import com.reportingservice.exception.EmailCouldNotBeSentException;
import com.reportingservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final String NO_REPLY_ADDRESS = "noreply@posProject.com";


    @Override
    public void sendEmailWithAttachment(String to,
                                        String subject,
                                        String body,
                                        ByteArrayInputStream attachmentBytes,
                                        String attachmentFilename) {
        log.trace("sendEmailWithAttachment method begins. to: {}, subject: {}, body: {}, filename: {}", to, subject, body, attachmentFilename);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setFrom(NO_REPLY_ADDRESS);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentBytes.readAllBytes()));

            mailSender.send(message);
            log.info("sendEmailWithAttachment: Email with attachment sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("sendEmailWithAttachment: Failed to send email with attachment to: {}", to, e);
            throw new EmailCouldNotBeSentException("Failed to send email with attachment to: " + to);
        }

        log.trace("sendEmailWithAttachment method ends. to: {}, subject: {}, body: {}, filename: {}", to, subject, body, attachmentFilename);
    }

    @Override
    public void sendEmail(String email,
                          String subject,
                          String body) {
        log.trace("sendEmail method begins. email: {}, subject: {}, body: {}", email, subject, body);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom(NO_REPLY_ADDRESS);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("sendEmail: Email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("sendEmail: Failed to send email to: {}", email, e);
            throw new EmailCouldNotBeSentException("Failed to send email to: " + email);
        }

        log.trace("sendEmail method ends. email: {}, subject: {}, body: {}", email, subject, body);
    }
}
