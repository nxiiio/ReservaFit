package cl.nxiiio.reservafit.events;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Logs the messages a producer sent that RabbitMQ could not deliver. Needs, in application.yaml,
// spring.rabbitmq.publisher-returns, publisher-confirm-type: correlated and template.mandatory.
@Configuration
public class PublisherLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(PublisherLoggingConfig.class);

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
