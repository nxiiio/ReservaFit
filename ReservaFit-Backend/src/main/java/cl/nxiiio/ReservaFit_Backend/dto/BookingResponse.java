package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import cl.nxiiio.ReservaFit_Backend.model.Gimnasio;
import cl.nxiiio.ReservaFit_Backend.model.Horario;
import cl.nxiiio.ReservaFit_Backend.model.Reserva;
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

    // Must run inside a transaction: gym and schedule are LAZY
    public static BookingResponse from(Reserva booking, boolean cancellable) {
        Gimnasio gym = booking.getGimnasio();
        Horario schedule = booking.getHorario();
        return new BookingResponse(
                booking.getId(),
                gym.getId(),
                gym.getNombre(),
                gym.getComuna(),
                gym.getImagenUrl(),
                schedule.getId(),
                schedule.getHoraInicio(),
                schedule.getHoraFin(),
                booking.getFecha(),
                booking.getEstado().name(),
                cancellable,
                booking.getFechaCreacion());
    }
}
