package cl.nxiiio.reservafit.gimnasios.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.nxiiio.reservafit.gimnasios.exception.GymNotFoundException;
import cl.nxiiio.reservafit.gimnasios.model.ReservaGimnasio;
import cl.nxiiio.reservafit.gimnasios.repository.GymBookingRepository;
import cl.nxiiio.reservafit.gimnasios.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Receives the bookings made in ms-reservas, so each gym keeps a record of its bookings
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingListener {

    private static final String CONFIRMED = "CONFIRMADA";

    private final GymRepository gymRepository;
    private final GymBookingRepository gymBookingRepository;

    // If this method throws, the message is retried and then sent to gimnasios.bookings.dlq
    @RabbitListener(queues = RabbitNames.BOOKINGS_QUEUE)
    public void onBookingEvent(BookingEvent event) {
        // A booking for a gym that does not exist here cannot be recorded
        if (!gymRepository.existsById(event.getGymId())) {
            throw new GymNotFoundException(event.getGymId());
        }

        ReservaGimnasio record = new ReservaGimnasio();
        record.setId(event.getBookingId());
        record.setUsuarioOid(event.getUserOid());
        record.setGimnasioId(event.getGymId());
        record.setHorarioId(event.getScheduleId());
        record.setFecha(event.getDate());
        record.setEstado(event.getStatus());

        // Same id as in ms-reservas: inserts the row when it is created, replaces it when it is canceled
        gymBookingRepository.save(record);

        long confirmed = gymBookingRepository.countByGimnasioIdAndEstado(event.getGymId(), CONFIRMED);

        log.info("Booking {} {}: gym={} schedule={} date={} user={} (the gym has {} confirmed bookings)",
                event.getBookingId(), event.getStatus(), event.getGymId(), event.getScheduleId(),
                event.getDate(), event.getUserOid(), confirmed);
    }
}
