package com.saleservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publisher class for sending messages to RabbitMQ exchanges.
 * Provides methods to publish different types of messages to specific exchanges and routing keys.
 * @author Emir Aktaş
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqMessagePublisher {

    @Value("${stock.rabbitmq.routingKey}") private String stockRoutingKey;
    @Value("${stock.rabbitmq.exchange}") private String stockExchange;

    @Value("${receipt.rabbitmq.routingKey}") private String receiptRoutingKey;
    @Value("${receipt.rabbitmq.exchange}") private String receiptExchange;

    @Value("${event.rabbitmq.exchange}") String eventExchange;
    @Value("${event.rabbitmq.routingKey}") String eventRoutingKey;

    private final AmqpTemplate amqpTemplate;


    /**
     * Publishes a message to the appropriate RabbitMQ exchange and routing key based on the message type.
     *
     * @param payload     the message payload to be published
     * @param messageType the type of the message, determining the target exchange and routing key
     * @throws IllegalArgumentException if the message type is unsupported
     */
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


    /**
     * Publishes an event message to the event exchange and routing key.
     * The event queue is used to immediately notify of messages written to the queue.
     *
     * @param saleId    the ID of the sale
     * @param requestId the ID of the request
     */
    public void publishEvent(Long saleId, String requestId){
        log.trace("publishEvent method begins. SaleId: {}, RequestId: {}", saleId, requestId);

        amqpTemplate.convertAndSend(eventExchange, eventRoutingKey, saleId + "." + requestId);
        log.info("publishEvent: Published to {} using routingKey {}. RequestId: {}, SaleId{}",
                eventExchange, eventRoutingKey, saleId, requestId);

        log.trace("publishEvent method ends. SaleId: {}, RequestId: {}", saleId, requestId);
    }


    /**
     * Enumeration for message types.
     */
    public enum MessageType {
        STOCK,
        RECEIPT
    }
}

