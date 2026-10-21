package cl.nxiiio.reservafit.reservas.messaging;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import cl.nxiiio.reservafit.reservas.model.EstadoReserva;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Sends the bookings to RabbitMQ (today ms-gimnasios reads them).
// BookingService only announces the change inside the application (publishEvent); this
// method runs once the database transaction has committed, so a booking that ends up not
// being saved never produces a message.
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener
    public void publish(BookingEvent event) {
        String routingKey = RabbitNames.BOOKING_CREATED;
        if (EstadoReserva.CANCELADA.name().equals(event.getStatus())) {
            routingKey = RabbitNames.BOOKING_CANCELLED;
        }

        try {
            rabbitTemplate.convertAndSend(RabbitNames.EVENTS_EXCHANGE, routingKey, event);

            log.info("Published {}: booking={} gym={}", routingKey, event.getBookingId(), event.getGymId());
        } catch (AmqpException e) {
            // The booking is already saved, so the user still gets their answer
            log.error("Could not publish {} for booking {}", routingKey, event.getBookingId(), e);
        }
    }
}
