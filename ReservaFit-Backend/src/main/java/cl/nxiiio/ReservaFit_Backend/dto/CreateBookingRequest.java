package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Selecciona un horario")
    private Long scheduleId;

    @NotNull(message = "Selecciona una fecha")
    private LocalDate date;
}
