package cl.nxiiio.reservafit.gimnasios.dto;

import java.time.LocalTime;

import cl.nxiiio.reservafit.gimnasios.model.Gimnasio;
import cl.nxiiio.reservafit.gimnasios.model.Horario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One schedule together with its gym. ms-reservas asks for it when a booking is made
// or listed, to check the start time and to show the gym's data.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailResponse {

    private Long id;
    private Long gymId;
    private String gymName;
    private String gymComuna;
    private String gymImageUrl;
    private LocalTime startTime;
    private LocalTime endTime;

    public static ScheduleDetailResponse from(Horario schedule) {
        Gimnasio gym = schedule.getGimnasio();

        return new ScheduleDetailResponse(
                schedule.getId(),
                gym.getId(),
                gym.getNombre(),
                gym.getComuna(),
                gym.getImagenUrl(),
                schedule.getHoraInicio(),
                schedule.getHoraFin());
    }
}
