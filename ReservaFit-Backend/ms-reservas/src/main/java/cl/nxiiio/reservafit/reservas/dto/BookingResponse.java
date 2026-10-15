package cl.nxiiio.reservafit.reservas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import cl.nxiiio.reservafit.reservas.model.HorarioReplica;
import cl.nxiiio.reservafit.reservas.model.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long gymId;
    private String gymName;
    private String gymComuna;
    private String gymImageUrl;
    private Long scheduleId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
    private String status;
    private boolean cancellable;
    private LocalDateTime createdAt;

    // Gym data comes from the schedule copy, since gyms live in ms-gimnasios
    public static BookingResponse from(Reserva booking, HorarioReplica schedule, boolean cancellable) {
        return new BookingResponse(
                booking.getId(),
                booking.getGimnasioId(),
                schedule.getGimnasioNombre(),
                schedule.getGimnasioComuna(),
                schedule.getGimnasioImagenUrl(),
                booking.getHorarioId(),
                schedule.getHoraInicio(),
                schedule.getHoraFin(),
                booking.getFecha(),
                booking.getEstado().name(),
                cancellable,
                booking.getFechaCreacion());
    }
}
