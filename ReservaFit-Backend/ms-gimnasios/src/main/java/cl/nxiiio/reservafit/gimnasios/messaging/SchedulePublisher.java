package cl.nxiiio.reservafit.gimnasios.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.events.RabbitNames;
import cl.nxiiio.reservafit.events.SchedulePublishedEvent;
import cl.nxiiio.reservafit.gimnasios.model.Gimnasio;
import cl.nxiiio.reservafit.gimnasios.model.Horario;
import cl.nxiiio.reservafit.gimnasios.repository.ScheduleRepository;

// Sends every schedule to the other services (today only ms-reservas) through RabbitMQ.
// Schedules only come from data.sql, so the moment to publish them is when this service starts.
@Component
public class SchedulePublisher {

    private static final Logger log = LoggerFactory.getLogger(SchedulePublisher.class);

    private final ScheduleRepository scheduleRepository;
    private final RabbitTemplate rabbitTemplate;

    public SchedulePublisher(ScheduleRepository scheduleRepository, RabbitTemplate rabbitTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Runs once, when ms-gimnasios has finished starting (data.sql has already run)
    @EventListener(ApplicationReadyEvent.class)
    public void publishAllSchedules() {
        List<Horario> schedules = scheduleRepository.findAllWithGym();

        try {
            for (Horario schedule : schedules) {
                rabbitTemplate.convertAndSend(
                        RabbitNames.EVENTS_EXCHANGE,
                        RabbitNames.SCHEDULE_PUBLISHED,
                        toEvent(schedule));
            }
            log.info("Published {} schedules ({})", schedules.size(), RabbitNames.SCHEDULE_PUBLISHED);
        } catch (AmqpException e) {
            // The service keeps working; restarting it publishes all the schedules again
            log.error("Could not publish the schedules to RabbitMQ", e);
        }
    }

    private SchedulePublishedEvent toEvent(Horario schedule) {
        Gimnasio gym = schedule.getGimnasio();

        return new SchedulePublishedEvent(
                schedule.getId(),
                gym.getId(),
                gym.getNombre(),
                gym.getComuna(),
                gym.getImagenUrl(),
                schedule.getHoraInicio(),
                schedule.getHoraFin());
    }
}
