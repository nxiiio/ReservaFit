package cl.nxiiio.ReservaFit_Backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.ReservaFit_Backend.dto.GymResponse;
import cl.nxiiio.ReservaFit_Backend.dto.ScheduleResponse;
import cl.nxiiio.ReservaFit_Backend.dto.SlotAvailabilityResponse;
import cl.nxiiio.ReservaFit_Backend.service.BookingService;
import cl.nxiiio.ReservaFit_Backend.service.GymService;

@RestController
@RequestMapping("/api/gimnasios")
public class GymController {

    private final GymService gymService;
    private final BookingService bookingService;

    public GymController(GymService gymService, BookingService bookingService) {
        this.gymService = gymService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<GymResponse> listGyms(@RequestParam(required = false) String comuna) {
        return gymService.findAll(comuna).stream()
                .map(GymResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GymResponse getGym(@PathVariable Long id) {
        return GymResponse.from(gymService.findById(id));
    }

    @GetMapping("/{id}/horarios")
    public List<ScheduleResponse> listSchedules(@PathVariable Long id) {
        return gymService.findSchedules(id).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @GetMapping("/{id}/disponibilidad")
    public List<SlotAvailabilityResponse> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return bookingService.getAvailability(id, fecha);
    }
}
