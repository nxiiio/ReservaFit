package cl.nxiiio.reservafit.reservas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.events.RabbitNames;
import cl.nxiiio.reservafit.events.SchedulePublishedEvent;
import cl.nxiiio.reservafit.reservas.model.HorarioReplica;
import cl.nxiiio.reservafit.reservas.repository.ScheduleReplicaRepository;

// Keeps horario_replica up to date with the schedules that ms-gimnasios publishes
@Component
public class ScheduleReplicaListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduleReplicaListener.class);

    private final ScheduleReplicaRepository repository;

    public ScheduleReplicaListener(ScheduleReplicaRepository repository) {
        this.repository = repository;
    }

    // If this method throws, the message is retried and then sent to reservas.schedules.dlq
    @RabbitListener(queues = RabbitNames.SCHEDULES_QUEUE)
    public void onSchedulePublished(SchedulePublishedEvent event) {
        HorarioReplica copy = new HorarioReplica();
        copy.setId(event.getScheduleId());
        copy.setGimnasioId(event.getGymId());
        copy.setGimnasioNombre(event.getGymName());
        copy.setGimnasioComuna(event.getGymComuna());
        copy.setGimnasioImagenUrl(event.getGymImageUrl());
        copy.setHoraInicio(event.getStartTime());
        copy.setHoraFin(event.getEndTime());

        // Same id as in ms-gimnasios: inserts the row the first time, replaces it afterwards
        repository.save(copy);

        log.info("Schedule copy saved: id={}", event.getScheduleId());
    }
}
