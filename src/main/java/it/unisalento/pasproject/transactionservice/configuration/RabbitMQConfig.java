package it.unisalento.pasproject.transactionservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ------  SECURITY  ------ //

    // Needed by authentication service
    @Value("${rabbitmq.queue.security.name}")
    private String securityResponseQueue;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    /*@Bean
    public Queue securityResponseQueue() {
        return new Queue(securityResponseQueue);
    }*/

    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange(securityExchange);
    }

    /*@Bean
    public Binding securityBinding() {
        return BindingBuilder
                .bind(securityResponseQueue())
                .to(securityExchange())
                .with(securityRequestRoutingKey);
    }*/

    // ------  END SECURITY  ------ //


    // ----- PAYMENT ----- //

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }

    @Value("${rabbitmq.queue.responseTransaction.name}")
    private String responseTransactionQueue;

    @Value("${rabbitmq.routing.responseTransaction.key}")
    private String responseTransactionKey;

    @Bean
    public Queue responseTransactionQueue() {
        return new Queue(responseTransactionQueue);
    }

    @Bean
    public Binding responseTransactionBinding() {
        return BindingBuilder
                .bind(responseTransactionQueue())
                .to(transactionExchange())
                .with(responseTransactionKey);
    }

    // ----- END PAYMENT ----- //

    // ----- TRANSACTION REQUEST & NOTIFY ----- //

    @Value("${rabbitmq.queue.receiveTransaction.name}")
    private String receiveTransactionQueue;

    @Value("${rabbitmq.routing.receiveTransaction.name}")
    private String receiveTransactionKey;

    @Bean
    public Queue receiveTransactionQueue() {
        return new Queue(receiveTransactionQueue);
    }

    @Bean
    public Binding receiveTransactionBinding() {
        return BindingBuilder
                .bind(receiveTransactionQueue())
                .to(transactionExchange())
                .with(receiveTransactionKey);
    }

    // ----- END TRANSACTION REQUEST & NOTIFY ----- //

    // ----- TRANSACTION REQUEST ----- //

    @Value("${rabbitmq.queue.requestTransaction.name}")
    private String requestTransactionQueue;

    @Value("${rabbitmq.routing.requestTransaction.key}")
    private String requestTransactionTopic;

    @Bean
    public Queue requestTransactionQueue() {
        return new Queue(requestTransactionQueue);
    }

    @Bean
    public Binding requestTransactionBinding() {
        return BindingBuilder
                .bind(requestTransactionQueue())
                .to(transactionExchange())
                .with(requestTransactionTopic);
    }

    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
