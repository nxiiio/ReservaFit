package cl.nxiiio.reservafit.events;

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
import org.springframework.context.annotation.Import;

// Declares the whole RabbitMQ topology. Every service imports this class, and Spring creates
// what is missing when it connects (declaring something that already exists does nothing).
// So it does not matter which service starts first: the queues always exist before a message.
// It also brings PublisherLoggingConfig, so importing this one class is all a service needs.
@Configuration
@Import(PublisherLoggingConfig.class)
public class RabbitTopologyConfig {

    // ---------- Exchanges ----------

    // Normal messages: routes by pattern (binding keys like "user.*")
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

    // ---------- Schedules ----------

    @Bean
    public Queue schedulesQueue() {
        return QueueBuilder.durable(RabbitNames.SCHEDULES_QUEUE)
                .deadLetterExchange(RabbitNames.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitNames.SCHEDULES_DLQ)
                .build();
    }

    @Bean
    public Binding schedulesBinding() {
        return BindingBuilder.bind(schedulesQueue())
                .to(eventsExchange())
                .with(RabbitNames.SCHEDULES_BINDING);
    }

    @Bean
    public Queue schedulesDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.SCHEDULES_DLQ).build();
    }

    @Bean
    public Binding schedulesDeadLetterBinding() {
        return BindingBuilder.bind(schedulesDeadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitNames.SCHEDULES_DLQ);
    }

    // ---------- Message format ----------

    // Messages travel as JSON. Spring Boot uses this bean for both sending and receiving.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
