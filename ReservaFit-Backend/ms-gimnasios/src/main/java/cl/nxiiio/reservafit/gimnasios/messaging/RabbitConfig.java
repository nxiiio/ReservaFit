package cl.nxiiio.reservafit.gimnasios.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Declares the queue this service reads, its dead letter queue and both exchanges.
// Spring creates what is missing when it connects (declaring something that exists does nothing).
// The queue belongs to this service because it is the one that consumes it.
@Configuration
public class RabbitConfig {

    // ---------- Exchanges ----------

    // Normal messages: routes by pattern (binding keys like "booking.*")
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(RabbitNames.EVENTS_EXCHANGE);
    }

    // Rejected messages: routes by exact name to each dead letter queue
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitNames.DEAD_LETTER_EXCHANGE);
    }

    // ---------- Bookings ----------

    // A message rejected here is re-sent to reservafit.dlx with the routing key "gimnasios.bookings.dlq"
    @Bean
    public Queue bookingsQueue() {
        return QueueBuilder.durable(RabbitNames.BOOKINGS_QUEUE)
                .deadLetterExchange(RabbitNames.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitNames.BOOKINGS_DLQ)
                .build();
    }

    @Bean
    public Binding bookingsBinding() {
        return BindingBuilder.bind(bookingsQueue())
                .to(eventsExchange())
                .with(RabbitNames.BOOKINGS_BINDING);
    }

    @Bean
    public Queue bookingsDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.BOOKINGS_DLQ).build();
    }

    @Bean
    public Binding bookingsDeadLetterBinding() {
        return BindingBuilder.bind(bookingsDeadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitNames.BOOKINGS_DLQ);
    }

    // ---------- Message format ----------

    // Messages travel as JSON. The producer's event class lives in another package, so the
    // message is always read into the class of the listener's parameter (this service's copy)
    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
