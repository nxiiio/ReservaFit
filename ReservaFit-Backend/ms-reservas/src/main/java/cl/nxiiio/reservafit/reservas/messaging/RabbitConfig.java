package cl.nxiiio.reservafit.reservas.messaging;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Declares the queue this service reads, its dead letter queue and both exchanges.
// Spring creates what is missing when it connects (declaring something that exists does nothing).
// The users queue belongs to this service because it is the one that consumes it; the
// bookings queue that this service publishes to belongs to ms-gimnasios.
@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    // ---------- Exchanges ----------

    // Normal messages: routes by pattern (binding keys like "user.*" or "booking.*")
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(RabbitNames.EVENTS_EXCHANGE);
    }

    // Rejected messages: routes by exact name to each dead letter queue
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitNames.DEAD_LETTER_EXCHANGE);
    }

    // ---------- Users ----------

    // A message rejected here is re-sent to reservafit.dlx with the routing key "reservas.users.dlq"
    @Bean
    public Queue usersQueue() {
        return QueueBuilder.durable(RabbitNames.USERS_QUEUE)
                .deadLetterExchange(RabbitNames.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitNames.USERS_DLQ)
                .build();
    }

    @Bean
    public Binding usersBinding() {
        return BindingBuilder.bind(usersQueue())
                .to(eventsExchange())
                .with(RabbitNames.USERS_BINDING);
    }

    @Bean
    public Queue usersDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.USERS_DLQ).build();
    }

    @Bean
    public Binding usersDeadLetterBinding() {
        return BindingBuilder.bind(usersDeadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitNames.USERS_DLQ);
    }

    // ---------- Message format ----------

    // Messages travel as JSON, both ways. The producer's event classes live in other packages,
    // so a message is always read into the class of the listener's parameter (this service's copy)
    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    // ---------- Undelivered messages (when publishing) ----------

    // Logs the messages RabbitMQ could not deliver. Needs, in application.yaml,
    // spring.rabbitmq.publisher-returns, publisher-confirm-type: correlated and template.mandatory.
    @Bean
    public RabbitTemplateCustomizer logUndeliveredMessages() {
        return rabbitTemplate -> {
            // No queue matched the routing key, so RabbitMQ sent the message back
            rabbitTemplate.setReturnsCallback(this::logReturnedMessage);

            // RabbitMQ did not accept the message (nack)
            rabbitTemplate.setConfirmCallback(this::logRejectedPublish);
        };
    }

    private void logReturnedMessage(ReturnedMessage returned) {
        String body = new String(returned.getMessage().getBody(), StandardCharsets.UTF_8);
        log.warn("Message without a destination queue: exchange={} routingKey={} reason={} body={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText(), body);
    }

    private void logRejectedPublish(CorrelationData correlation, boolean ack, String cause) {
        if (!ack) {
            log.error("RabbitMQ did not confirm a published message: cause={}", cause);
        }
    }
}
