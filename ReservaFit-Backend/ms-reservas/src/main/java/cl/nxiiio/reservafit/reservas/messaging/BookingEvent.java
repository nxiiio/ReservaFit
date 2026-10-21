package cl.nxiiio.reservafit.reservas.messaging;

import java.time.LocalDate;
import java.time.LocalTime;

import cl.nxiiio.reservafit.reservas.client.ScheduleInfo;
import cl.nxiiio.reservafit.reservas.model.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sent when a booking is created (booking.created) or canceled (booking.cancelled).
// ms-gimnasios and ms-notificaciones have a copy of this class with the same fields: the
// message travels as JSON, so only the field names must match. It carries the gym name and
// times so the consumers never have to ask ms-gimnasios for them.
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

    private String gymName;
    private LocalTime startTime;
    private LocalTime endTime;

    // "CONFIRMADA" or "CANCELADA"
    private String status;

    public static BookingEvent from(Reserva booking, ScheduleInfo schedule) {
        return new BookingEvent(
                booking.getId(),
                booking.getUsuarioOid(),
                booking.getGimnasioId(),
                booking.getHorarioId(),
                booking.getFecha(),
                schedule.getGymName(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                booking.getEstado().name());
    }
}
