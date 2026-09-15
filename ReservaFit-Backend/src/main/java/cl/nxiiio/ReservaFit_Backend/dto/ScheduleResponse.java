package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalTime;

import cl.nxiiio.ReservaFit_Backend.model.Horario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;

    public static ScheduleResponse from(Horario schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getHoraInicio(),
                schedule.getHoraFin());
    }
}
