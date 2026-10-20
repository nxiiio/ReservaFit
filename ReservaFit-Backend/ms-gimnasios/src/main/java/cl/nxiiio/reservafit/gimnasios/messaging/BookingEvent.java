package cl.nxiiio.reservafit.gimnasios.messaging;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sent by ms-reservas when a booking is created (booking.created) or canceled (booking.cancelled).
// ms-reservas has a copy of this class with the same fields: the message travels as JSON,
// so only the field names must match.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    // ms-reservas' own booking id: used as the id of this service's record
    private Long bookingId;

    private Long userId;
    private Long gymId;
    private Long scheduleId;
    private LocalDate date;

    // "CONFIRMADA" or "CANCELADA"
    private String status;
}
