package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotAvailabilityResponse {

    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
