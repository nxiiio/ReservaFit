package cl.nxiiio.reservafit.usuarios.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.usuarios.model.Usuario;

// Tells the other services (today only ms-reservas) about a user, through RabbitMQ
@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishRegistered(Usuario user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getMicrosoftOid(),
                user.getNombre(),
                user.getEmail());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitNames.EVENTS_EXCHANGE,
                    RabbitNames.USER_REGISTERED,
                    event);
        } catch (AmqpException e) {
            // The login must still work when RabbitMQ is down: the copy in ms-reservas
            // is fixed on the user's next login, which publishes the snapshot again
            log.error("Could not publish {} for user {}", RabbitNames.USER_REGISTERED, user.getId(), e);
        }
    }
}
