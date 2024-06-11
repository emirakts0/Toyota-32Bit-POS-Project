package com.productservice.consumer;

import com.productservice.dto.StockUpdateMessage;
import com.productservice.service.ProductManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ProductConsumer {

    private final ProductManagementService productManagementService;
    private final AmqpTemplate rabbitTemplate;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Value("${stock.rabbitmq.queue}")
    private String queueName;

    @Value("${retry.limit}")
    private int retryLimit;


    @RabbitListener(queues = "${stock.rabbitmq.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeMessage(StockUpdateMessage message) {
        log.trace("consumeMessage method begins. Barcode: {}", message.getBarcode());

        try {
            log.info("consumeMessage: updating stock for barcode: {}", message.getBarcode());
            productManagementService.updateStock(message.getBarcode(), message.getStock());
        } catch (Exception e) {
            log.warn("consumeMessage: Error updating stock for barcode {}: {}", message.getBarcode(), e.getMessage(), e);
            processRetry(message);
        }

        log.trace("consumeMessage method ends. Barcode: {}", message.getBarcode());
    }

    private void processRetry(StockUpdateMessage message) {
        log.trace("processRetry method begins. retryCount: {}", message.getRetryCount());

        int retryCount = message.getRetryCount();
        if (retryCount < retryLimit) {
            log.info("processRetry: Retrying message for barcode {}. Attempt #{}", message.getBarcode(), retryCount + 1);
            requeueMessageWithDelay(message);
        } else {
            log.warn("processRetry: Retry limit reached for message with barcode {}. No more retries will be attempted.", message.getBarcode());
            handleFailedMessage(message);
        }

        log.trace("processRetry method ends. retryCount: {}", message.getRetryCount());
    }

    @Async
    public void requeueMessageWithDelay(StockUpdateMessage message) {
        taskExecutor.execute(() -> {
            log.trace("requeueMessageWithDelay task begins. Barcode: {}, Stock: {}", message.getBarcode(), message.getStock());

            try {
                Thread.sleep(5000);  // 5 second delay before requeue
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("requeueMessageWithDelay: Interrupted during sleep before retrying message for barcode {}", message.getBarcode(), ie);
            }
            message.setRetryCount(message.getRetryCount() + 1);
            rabbitTemplate.convertAndSend(queueName, message);
            log.info("requeueMessageWithDelay: Message requeued for barcode {} after delay. Retry count is now {}.", message.getBarcode(), message.getRetryCount());

            log.trace("requeueMessageWithDelay task ends. Barcode: {}, Stock: {}", message.getBarcode(), message.getStock());
        });
    }

    private void handleFailedMessage(StockUpdateMessage message) {
        log.error("handleFailedMessage: Handling failed message after max retries for barcode {}: {}", message.getBarcode(), message);
    }


    public ProductConsumer(ProductManagementService productManagementService, AmqpTemplate rabbitTemplate) {
        this.productManagementService = productManagementService;
        this.rabbitTemplate = rabbitTemplate;
    }
}
