package cl.nxiiio.reservafit.reservas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import cl.nxiiio.reservafit.reservas.client.ScheduleInfo;
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

    // Gym data comes from ms-gimnasios, together with the schedule (see GymClient)
    public static BookingResponse from(Reserva booking, ScheduleInfo schedule, boolean cancellable) {
        return new BookingResponse(
                booking.getId(),
                booking.getGimnasioId(),
                schedule.getGymName(),
                schedule.getGymComuna(),
                schedule.getGymImageUrl(),
                booking.getHorarioId(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                booking.getFecha(),
                booking.getEstado().name(),
                cancellable,
                booking.getFechaCreacion());
    }
}
