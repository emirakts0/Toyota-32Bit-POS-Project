package com.reportingservice.consumer;

import com.reportingservice.dto.ReceiptMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.service.PdfGenerationService;
import com.reportingservice.service.ReceiptTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer class for handling receipt messages from RabbitMQ queues.
 * Processes messages to generate PDF receipts and track their status.
 * @author Emir Aktaş
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptConsumer {

    private final PdfGenerationService pdfGenerationService;
    private final ReceiptTrackingService receiptTrackingService;


    /**
     * Consumes receipt messages from the RabbitMQ queue and generates a PDF receipt.
     *
     * @param message the receipt message containing sale details and request ID
     */
    @RabbitListener(queues = "${receipt.rabbitmq.queue}")
    public void consumeReceiptMessage(ReceiptMessage message) {
        log.trace("consumeReceiptMessage method begins. RequestId: {}", message.getRequestId());

        String requestId = message.getRequestId();
        SaleDto saleDto = message.getSaleDto();

        try {
            byte[] receiptBytes = pdfGenerationService.generateReceiptPDF(saleDto);
            receiptTrackingService.updateReceiptStatus(requestId, "COMPLETED", receiptBytes);
            log.info("consumeReceiptMessage: Receipt generation completed for RequestId: {}", requestId);
        } catch (Exception e) {
            log.error("consumeReceiptMessage: Error generating receipt for request ID {}: {}", requestId, e.getMessage());
            receiptTrackingService.updateReceiptStatus(requestId, "FAILED", null);
        }

        log.trace("consumeReceiptMessage method ends. RequestId: {}", requestId);
    }


    /**
     * Consumes event messages from the RabbitMQ event queue to initialize receipt cache.
     *
     * @param message the event message containing sale ID and event ID
     */
    @RabbitListener(queues = "${event.rabbitmq.queue}")
    public void consumeEventMessage(String message) {
        log.trace("consumeEventMessage method begins. Message: {}", message);

        String[] parts = message.split("\\.", 2);
        Long saleId = Long.parseLong(parts[0]);
        String eventId = parts[1];
        log.info("consumeEventMessage: Received message from RabbitMQ event. requestId: {}", eventId);

        receiptTrackingService.initializeReceiptCache(saleId, eventId);
        log.info("consumeEventMessage: Initialized receipt cache for SaleId: {} with RequestId: {}", saleId, eventId);

        log.trace("consumeEventMessage method ends. Message: {}", message);
    }
}
