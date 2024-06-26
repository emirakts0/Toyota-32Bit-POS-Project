package com.reportingservice.service.impl;

import com.reportingservice.exception.EmailCouldNotBeSentException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;


    @Test
    void whenSendEmailWithAttachmentWithValidParameters_thenSentMailSuccessfully() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ByteArrayInputStream attachmentBytes = new ByteArrayInputStream("test content".getBytes());

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmailWithAttachment("test@example.com", "Test Subject", "Test Body", attachmentBytes, "test.txt");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void whenSendEmailWithAttachmentWithMessagingProblems_thenThrowEmailCouldNotBeSentException() {
        ByteArrayInputStream attachmentBytes = new ByteArrayInputStream("test content".getBytes());

        doThrow(new RuntimeException()).when(mailSender).createMimeMessage();

        assertThrows(EmailCouldNotBeSentException.class,
                () -> emailService.sendEmailWithAttachment("test@example.com", "Test Subject", "Test Body", attachmentBytes, "test.txt"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void whenSendEmailWithValidParameters_thenSentMailSuccessfully() {
        emailService.sendEmail("test@example.com", "Test Subject", "Test Body");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void whenSendEmailWithMessagingProblems_thenThrowEmailCouldNotBeSentException() {
        doThrow(new RuntimeException()).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailCouldNotBeSentException.class,
                () -> emailService.sendEmail("test@example.com", "Test Subject", "Test Body"));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}