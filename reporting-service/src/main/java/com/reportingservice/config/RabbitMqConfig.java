package com.reportingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${receipt.rabbitmq.exchange}") String receiptExchange;
    @Value("${receipt.rabbitmq.queue}") String receiptQueueName;
    @Value("${receipt.rabbitmq.routingKey}") String receiptRoutingKey;

    @Value("${event.rabbitmq.exchange}") String eventExchange;
    @Value("${event.rabbitmq.queue}") String eventQueueName;
    @Value("${event.rabbitmq.routingKey}") String eventRoutingKey;

    @Value("${excel.rabbitmq.exchange}") String excelExchange;
    @Value("${excel.rabbitmq.queue}") String excelQueueName;
    @Value("${excel.rabbitmq.routingKey}") String excelRoutingKey;


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
    DirectExchange excelExchange() { return new DirectExchange(excelExchange); }
    @Bean
    Queue excelQueue() { return new Queue(excelQueueName, false); }
    @Bean
    Binding excelBinding(Queue excelQueue, DirectExchange excelExchange) {
        return BindingBuilder.bind(excelQueue).to(excelExchange).with(excelRoutingKey); }


    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        //factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
