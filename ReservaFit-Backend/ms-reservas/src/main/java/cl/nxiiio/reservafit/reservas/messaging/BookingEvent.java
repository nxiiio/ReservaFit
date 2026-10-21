package cl.nxiiio.reservafit.reservas.messaging;

import java.time.LocalDate;

import cl.nxiiio.reservafit.reservas.model.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sent when a booking is created (booking.created) or canceled (booking.cancelled).
// ms-gimnasios has a copy of this class with the same fields: the message travels as JSON,
// so only the field names must match.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    private Long bookingId;
    // The user's Microsoft oid
    private String userOid;
    private Long gymId;
    private Long scheduleId;
    private LocalDate date;

    // "CONFIRMADA" or "CANCELADA"
    private String status;

    public static BookingEvent from(Reserva booking) {
        return new BookingEvent(
                booking.getId(),
                booking.getUsuarioOid(),
                booking.getGimnasioId(),
                booking.getHorarioId(),
                booking.getFecha(),
                booking.getEstado().name());
    }
}
