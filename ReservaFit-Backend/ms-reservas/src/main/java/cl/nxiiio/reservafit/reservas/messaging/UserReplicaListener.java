package cl.nxiiio.reservafit.reservas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.events.RabbitNames;
import cl.nxiiio.reservafit.events.UserRegisteredEvent;
import cl.nxiiio.reservafit.reservas.model.UsuarioReplica;
import cl.nxiiio.reservafit.reservas.repository.UserReplicaRepository;

// Keeps usuario_replica up to date with the users that ms-usuarios publishes
@Component
public class UserReplicaListener {

    private static final Logger log = LoggerFactory.getLogger(UserReplicaListener.class);

    private final UserReplicaRepository repository;

    public UserReplicaListener(UserReplicaRepository repository) {
        this.repository = repository;
    }

    // If this method throws, the message is retried and then sent to reservas.users.dlq
    @RabbitListener(queues = RabbitNames.USERS_QUEUE)
    public void onUserRegistered(UserRegisteredEvent event) {
        UsuarioReplica copy = new UsuarioReplica();
        copy.setId(event.getUserId());
        copy.setMicrosoftOid(event.getMicrosoftOid());
        copy.setNombre(event.getName());
        copy.setEmail(event.getEmail());

        // Same id as in ms-usuarios: inserts the row the first time, replaces it afterwards
        repository.save(copy);

        log.info("User copy saved: id={}", event.getUserId());
    }
}
