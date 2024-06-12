package com.saleservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqMessagePublisher {

    @Value("${stock.rabbitmq.routingKey}") private String stockRoutingKey;
    @Value("${stock.rabbitmq.exchange}") private String stockExchange;

    @Value("${receipt.rabbitmq.routingKey}") private String receiptRoutingKey;
    @Value("${receipt.rabbitmq.exchange}") private String receiptExchange;

    @Value("${rabbitmq.event.exchange}") String eventExchange;
    @Value("${rabbitmq.event.routingKey}") String eventRoutingKey;


    private final AmqpTemplate amqpTemplate;

    public void publishMessage(Object payload, MessageType messageType) {
        log.trace("publishMessage method begins. messageType={}", messageType);

        String exchange;
        String routingKey;

        switch (messageType) {
            case STOCK:
                exchange = stockExchange;
                routingKey = stockRoutingKey;
                break;
            case RECEIPT:
                exchange = receiptExchange;
                routingKey = receiptRoutingKey;
                break;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }

        log.info("publishMessage: Publishing to {} using routingKey {}. messageType: {}", exchange, routingKey, messageType);
        amqpTemplate.convertAndSend(exchange, routingKey, payload);
        log.info("publishMessage: Published to {} using routingKey {}. messageType: {}", exchange, routingKey, messageType);

        log.trace("publishMessage method ends. messageType={}", messageType);
    }


    public void publishEvent(Long saleId, String requestId){
        log.trace("publishEvent method begins. SaleId: {}, RequestId: {}", saleId, requestId);

        log.info("publishMessage: Publishing to {} using routingKey {}. RequestId: {}, SaleId{}",
                eventExchange, eventRoutingKey, saleId, requestId);
        amqpTemplate.convertAndSend(eventExchange, eventRoutingKey, saleId + "." + requestId);
        log.info("publishMessage: Published to {} using routingKey {}. RequestId: {}, SaleId{}",
                eventExchange, eventRoutingKey, saleId, requestId);

        log.trace("publishEvent method ends. SaleId: {}, RequestId: {}", saleId, requestId);
    }


    public enum MessageType {
        STOCK,
        RECEIPT
    }
}

