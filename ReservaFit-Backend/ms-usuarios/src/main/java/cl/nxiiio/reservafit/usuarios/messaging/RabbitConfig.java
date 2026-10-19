package cl.nxiiio.reservafit.usuarios.messaging;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ setup of this service, which only publishes. The queues belong to ms-reservas,
// which declares them; this service only needs the exchange it publishes to.
@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    // Created in RabbitMQ on connect if it does not exist yet
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(RabbitNames.EVENTS_EXCHANGE);
    }

    // Messages travel as JSON. Spring Boot uses this bean when sending.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

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
