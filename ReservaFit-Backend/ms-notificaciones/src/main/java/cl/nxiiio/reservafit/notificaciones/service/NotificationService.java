package cl.nxiiio.reservafit.notificaciones.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.reservafit.notificaciones.dto.NotificationResponse;
import cl.nxiiio.reservafit.notificaciones.exception.NotificationNotFoundException;
import cl.nxiiio.reservafit.notificaciones.messaging.BookingEvent;
import cl.nxiiio.reservafit.notificaciones.model.Notificacion;
import cl.nxiiio.reservafit.notificaciones.model.TipoNotificacion;
import cl.nxiiio.reservafit.notificaciones.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final Locale SPANISH = Locale.forLanguageTag("es-CL");

    // "martes 21 de octubre" and "18:00"
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", SPANISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationRepository repository;

    // Called for every booking message. Throwing here sends the message to the DLQ.
    @Transactional
    public void createFrom(BookingEvent event) {
        if (event.getBookingId() == null || event.getUserOid() == null
                || event.getDate() == null || event.getStartTime() == null) {
            throw new IllegalArgumentException("Incomplete booking message: booking=" + event.getBookingId());
        }

        TipoNotificacion type = TipoNotificacion.RESERVA_CREADA;
        if ("CANCELADA".equals(event.getStatus())) {
            type = TipoNotificacion.RESERVA_CANCELADA;
        }

        // RabbitMQ may deliver the same message twice: the second one changes nothing
        if (repository.existsByReservaIdAndTipo(event.getBookingId(), type)) {
            log.info("Notification already exists, skipping: booking={} type={}", event.getBookingId(), type);
            return;
        }

        String when = event.getDate().format(DAY_FORMAT) + " a las " + event.getStartTime().format(TIME_FORMAT);
        String message = "Reservaste en " + event.getGymName() + " el " + when;
        if (type == TipoNotificacion.RESERVA_CANCELADA) {
            message = "Cancelaste tu reserva en " + event.getGymName() + " del " + when;
        }

        Notificacion notification = new Notificacion();
        notification.setUsuarioOid(event.getUserOid());
        notification.setReservaId(event.getBookingId());
        notification.setTipo(type);
        notification.setMensaje(message);
        notification.setLeida(false);
        repository.save(notification);

        log.info("Notification saved: booking={} type={} user={}", event.getBookingId(), type, event.getUserOid());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findMine(String oid) {
        return repository.findByUsuarioOidOrderByFechaCreacionDesc(oid).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(String oid, Long id) {
        Notificacion notification = repository.findByIdAndUsuarioOid(id, oid)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.setLeida(true);
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllRead(String oid) {
        repository.findByUsuarioOidAndLeidaFalse(oid)
                .forEach(notification -> notification.setLeida(true));
    }
}
