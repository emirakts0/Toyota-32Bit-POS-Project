package com.reportingservice.consumer;

import com.reportingservice.dto.ReceiptMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.service.PdfGenerationService;
import com.reportingservice.service.ReceiptTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ReceiptConsumer {

    private final PdfGenerationService pdfGenerationService;
    private final ReceiptTrackingService receiptTrackingService;

    public ReceiptConsumer(PdfGenerationService pdfGenerationService, ReceiptTrackingService receiptTrackingService) {
        this.pdfGenerationService = pdfGenerationService;
        this.receiptTrackingService = receiptTrackingService;
    }

    @RabbitListener(queues = "${receipt.rabbitmq.queue}")
    public void consumeMessage(ReceiptMessage message) {
        log.trace("consumeMessage method begins. RequestId: {}", message.getRequestId());

        String requestId = message.getRequestId();
        try {
            SaleDto saleDto = message.getSaleDto();

            //TODO: remove. (Simulating delay for processing)
            Thread.sleep(10000);

            byte[] receiptBytes = pdfGenerationService.generateReceiptPDF(saleDto);
            receiptTrackingService.updateReceiptStatus(requestId, "COMPLETED", receiptBytes);
            log.info("consumeMessage: Receipt generation completed for RequestId: {}", requestId);
        } catch (Exception e) {
            log.error("consumeMessage: Error generating receipt for request ID {}: {}", requestId, e.getMessage());
            receiptTrackingService.updateReceiptStatus(requestId, "FAILED", null);
        }

        log.trace("consumeMessage method ends. RequestId: {}", requestId);
    }

    @RabbitListener(queues = "${rabbitmq.event.queue}")
    public void handleQueueEvent(String message) {
        log.trace("handleQueueEvent method begins. Message: {}", message);

        String[] parts = message.split("\\.", 2);
        Long saleId = Long.parseLong(parts[0]);
        String eventId = parts[1];
        log.info("handleQueueEvent: Received message from RabbitMQ event. requestId: {}", eventId);

        receiptTrackingService.initializeReceiptCache(saleId, eventId);
        log.info("Initialized receipt cache for SaleId: {} with RequestId: {}", saleId, eventId);

        log.trace("handleQueueEvent method ends. Message: {}", message);
    }
}


