package com.reportingservice.service;

import com.reportingservice.exception.EmailCouldNotBeSentException;

import java.io.ByteArrayInputStream;

/**
 * Service interface for sending emails.
 * Provides methods for sending emails with and without attachments.
 * @author Emir Aktaş
 */
public interface EmailService {

    /**
     * Sends an email with an attachment.
     *
     * @param to the recipient email address
     * @param subject the subject of the email
     * @param body the body of the email
     * @param attachmentBytes the attachment content as a ByteArrayInputStream
     * @param attachmentFilename the filename of the attachment
     * @throws EmailCouldNotBeSentException if the email with attachment could not be sent
     */
    void sendEmailWithAttachment(String to,
                                 String subject,
                                 String body,
                                 ByteArrayInputStream attachmentBytes,
                                 String attachmentFilename);


    /**
     * Sends a simple email without an attachment.
     *
     * @param to the recipient email address
     * @param subject the subject of the email
     * @param body the body of the email
     * @throws EmailCouldNotBeSentException if the email could not be sent
     */
    void sendEmail(String to, String subject, String body);
}
