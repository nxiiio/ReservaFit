package cl.nxiiio.reservafit.reservas.client;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A schedule as ms-gimnasios returns it. GET /api/gimnasios/horarios/{id} fills every field;
// GET /api/gimnasios/{id}/horarios only fills id, startTime and endTime (the rest stay null).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInfo {

    private Long id;
    private Long gymId;
    private String gymName;
    private String gymComuna;
    private String gymImageUrl;
    private LocalTime startTime;
    private LocalTime endTime;
}
