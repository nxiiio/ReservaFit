package cl.nxiiio.reservafit.reservas.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.reservas.model.UsuarioReplica;
import cl.nxiiio.reservafit.reservas.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Keeps usuario_replica up to date with the users that ms-usuarios publishes
@Component
@Slf4j
@RequiredArgsConstructor
public class UserReplicaListener {

    private final UserReplicaRepository repository;

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
