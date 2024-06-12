package com.saleservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${stock.rabbitmq.exchange}") String stockExchange;
    @Value("${stock.rabbitmq.queue}") String stockQueueName;
    @Value("${stock.rabbitmq.routingKey}") String stockRoutingKey;

    @Value("${receipt.rabbitmq.exchange}") String receiptExchange;
    @Value("${receipt.rabbitmq.queue}") String receiptQueueName;
    @Value("${receipt.rabbitmq.routingKey}") String receiptRoutingKey;

    @Value("${rabbitmq.event.exchange}") String eventExchange;
    @Value("${rabbitmq.event.queue}") String eventQueueName;
    @Value("${rabbitmq.event.routingKey}") String eventRoutingKey;


    @Bean
    DirectExchange stockExchange() { return new DirectExchange(stockExchange); }
    @Bean
    Queue stockQueue(){ return new Queue(stockQueueName, false); }
    @Bean
    Binding stockBinding(Queue stockQueue, DirectExchange stockExchange){
        return BindingBuilder.bind(stockQueue).to(stockExchange).with(stockRoutingKey); }


    @Bean
    DirectExchange receiptExchange() { return new DirectExchange(receiptExchange); }
    @Bean
    Queue receiptQueue(){ return new Queue(receiptQueueName, false); }
    @Bean
    Binding receiptBinding(Queue receiptQueue, DirectExchange receiptExchange){
        return BindingBuilder.bind(receiptQueue).to(receiptExchange).with(receiptRoutingKey); }


    @Bean
    DirectExchange eventExchange() { return new DirectExchange(eventExchange); }
    @Bean
    Queue eventQueue() { return new Queue(eventQueueName, false);}
    @Bean
    Binding eventBinding(Queue eventQueue, DirectExchange eventExchange) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with(eventRoutingKey);}


    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
