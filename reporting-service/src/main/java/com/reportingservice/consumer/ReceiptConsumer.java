package com.reportingservice.consumer;

import com.reportingservice.dto.ReceiptMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.service.PdfGenerationService;
import com.reportingservice.service.ReceiptTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptConsumer {

    private final PdfGenerationService pdfGenerationService;
    private final ReceiptTrackingService receiptTrackingService;


    @RabbitListener(queues = "${receipt.rabbitmq.queue}")
    public void consumeReceiptMessage(ReceiptMessage message) {
        log.trace("consumeReceiptMessage method begins. RequestId: {}", message.getRequestId());

        String requestId = message.getRequestId();
        SaleDto saleDto = message.getSaleDto();

        try {
            //TODO: remove. (Simulating delay for processing)
            Thread.sleep(10000);

            byte[] receiptBytes = pdfGenerationService.generateReceiptPDF(saleDto);
            receiptTrackingService.updateReceiptStatus(requestId, "COMPLETED", receiptBytes);
            log.info("consumeReceiptMessage: Receipt generation completed for RequestId: {}", requestId);
        } catch (Exception e) {
            log.error("consumeReceiptMessage: Error generating receipt for request ID {}: {}", requestId, e.getMessage());
            receiptTrackingService.updateReceiptStatus(requestId, "FAILED", null);
        }

        log.trace("consumeReceiptMessage method ends. RequestId: {}", requestId);
    }

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


