package com.reportingservice.consumer;

import com.reportingservice.dto.ExcelReportMessage;
import com.reportingservice.service.EmailService;
import com.reportingservice.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ExcelReportConsumer {

    private final ExcelService excelService;
    private final EmailService emailService;
    private final AmqpTemplate rabbitTemplate;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Value("${excel.rabbitmq.queue}")
    private String queueName;

    @Value("${retry.limit}")
    private int retryLimit;


    @RabbitListener(queues = "${excel.rabbitmq.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeExcelMessage(ExcelReportMessage message) {
        log.trace("consumeExcelMessage method begins. Email: {}", message.getMail());

        String mail = message.getMail();
        try {
            ByteArrayInputStream excelBytes = excelService.generateSalesExcelReport(message.getCriteria());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "sales_report_" + timestamp + ".xlsx";

            emailService.sendEmailWithAttachment(
                    mail,
                    "Sales Report",
                    "Find attached the sales report.",
                    excelBytes,
                    filename);
        } catch (Exception e) {
            log.warn("consumeExcelMessage: Exception while generating excel report for send email {}: {}", mail, e.getMessage(), e);
            processRetry(message);
        }

        log.trace("consumeExcelMessage method ends. Email: {}", mail);
    }

    private void processRetry(ExcelReportMessage message) {
        log.trace("processRetry method begins. retryCount: {}", message.getRetryCount());

        int retryCount = message.getRetryCount();
        if (retryCount < retryLimit) {
            log.info("processRetry: Retrying message for email {}. Attempt #{}", message.getMail(), retryCount + 1);
            requeueMessageWithDelay(message);
        } else {
            log.warn("processRetry: Retry limit reached for message with email {}. No more retries will be attempted.", message.getMail());
            handleFailedMessage(message);
        }

        log.trace("processRetry method ends. retryCount: {}", message.getRetryCount());
    }

    @Async
    public void requeueMessageWithDelay(ExcelReportMessage message) {
        taskExecutor.execute(() -> {
            log.trace("requeueMessageWithDelay task begins. Email: {}", message.getMail());

            String mail = message.getMail();
            try {
                Thread.sleep(5000);  // 5 second delay before requeue
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("requeueMessageWithDelay: Interrupted during sleep before retrying message for email {}", mail, ie);
            }
            message.setRetryCount(message.getRetryCount() + 1);
            rabbitTemplate.convertAndSend(queueName, message);
            log.info("requeueMessageWithDelay: Message requeued for email {} after delay. Retry count is now {}.", mail, message.getRetryCount());

            log.trace("requeueMessageWithDelay task ends. Email: {}", mail);
        });
    }

    private void handleFailedMessage(ExcelReportMessage message) {
        log.error("handleFailedMessage: Handling failed message after max retries for email {}: {}", message.getMail(), message);
        emailService.sendEmail(message.getMail(), "Failed to generate Excel report", "Failed to generate Excel report after multiple attempts.");
    }

    public ExcelReportConsumer(ExcelService excelService, EmailService emailService, AmqpTemplate rabbitTemplate) {
        this.excelService = excelService;
        this.emailService = emailService;
        this.rabbitTemplate = rabbitTemplate;
    }
}

