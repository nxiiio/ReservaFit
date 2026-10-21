package cl.nxiiio.reservafit.reservas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.reservafit.reservas.dto.SlotAvailabilityResponse;
import cl.nxiiio.reservafit.reservas.service.BookingService;
import lombok.RequiredArgsConstructor;

// Lives under /api/gimnasios but in this service, because it needs the bookings.
// The gateway routes this one path here and the rest of /api/gimnasios to ms-gimnasios.
@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final BookingService bookingService;

    @GetMapping("/api/gimnasios/{id}/disponibilidad")
    public List<SlotAvailabilityResponse> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return bookingService.getAvailability(id, fecha);
    }
}
