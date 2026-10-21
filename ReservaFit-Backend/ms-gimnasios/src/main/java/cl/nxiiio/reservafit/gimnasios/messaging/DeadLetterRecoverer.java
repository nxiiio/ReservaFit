package cl.nxiiio.reservafit.gimnasios.messaging;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// Runs when a message has failed every retry (see spring.rabbitmq.listener.simple.retry).
// It logs the message and then rejects it without sending it back to its queue, so RabbitMQ
// moves it to that queue's dead letter queue (gimnasios.bookings.dlq).
// The message stays there, visible in the RabbitMQ panel, so it can be inspected or re-sent.
// Spring Boot uses this bean automatically because it is the only MessageRecoverer.
@Component
@Slf4j
public class DeadLetterRecoverer extends RejectAndDontRequeueRecoverer {

    @Override
    public void recover(Message message, Throwable cause) {
        String queue = message.getMessageProperties().getConsumerQueue();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.error("Message failed after all retries, moving it to the dead letter queue: "
                + "queue={} routingKey={} body={} cause={}",
                queue, routingKey, body, rootCauseMessage(cause));

        // Rejects without requeue: this is what sends the message to the dead letter exchange
        super.recover(message, cause);
    }

    private String rootCauseMessage(Throwable cause) {
        Throwable root = cause;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}
