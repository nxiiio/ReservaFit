package cl.nxiiio.reservafit.reservas.messaging;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Copy of the message class of the publishing service (same field names). Published by ms-gimnasios for each schedule when it starts. Carries the gym data that
// booking responses show, so ms-reservas never has to ask ms-gimnasios for it.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePublishedEvent {

    // ms-gimnasios' own horario id: ms-reservas uses it as the id of its copy
    private Long scheduleId;

    private Long gymId;
    private String gymName;
    private String gymComuna;
    private String gymImageUrl;

    private LocalTime startTime;
    private LocalTime endTime;
}
