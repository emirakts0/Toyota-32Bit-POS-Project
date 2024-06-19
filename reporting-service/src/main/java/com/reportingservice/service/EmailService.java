package com.reportingservice.service;

import java.io.ByteArrayInputStream;

public interface EmailService {

    /**
     * Sends an email with an attachment.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param body the body content of the email
     * @param attachmentBytes the attachment content as a ByteArrayInputStream
     * @param attachmentFilename the filename of the attachment
     */
    void sendEmailWithAttachment(String to,
                                 String subject,
                                 String body,
                                 ByteArrayInputStream attachmentBytes,
                                 String attachmentFilename);

    /**
     * Sends a simple email without attachment.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param body the body content of the email
     */
    void sendEmail(String to, String subject, String body);
}

