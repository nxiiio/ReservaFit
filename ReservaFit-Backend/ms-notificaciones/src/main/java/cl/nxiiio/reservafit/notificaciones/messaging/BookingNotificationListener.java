package cl.nxiiio.reservafit.notificaciones.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.notificaciones.service.NotificationService;
import lombok.RequiredArgsConstructor;

// Turns every booking made or canceled in ms-reservas into a notification for its user
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final NotificationService notificationService;

    // If this method throws, the message is retried and then sent to notificaciones.bookings.dlq
    @RabbitListener(queues = RabbitNames.BOOKINGS_QUEUE)
    public void onBookingEvent(BookingEvent event) {
        notificationService.createFrom(event);
    }
}
