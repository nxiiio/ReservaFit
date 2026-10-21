package cl.nxiiio.reservafit.gimnasios.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.reservafit.gimnasios.dto.GymResponse;
import cl.nxiiio.reservafit.gimnasios.dto.ScheduleDetailResponse;
import cl.nxiiio.reservafit.gimnasios.dto.ScheduleResponse;
import cl.nxiiio.reservafit.gimnasios.service.GymService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gimnasios")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

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

    // Used by ms-reservas, which calls this service directly (not through the gateway)
    @GetMapping("/horarios/{scheduleId}")
    public ScheduleDetailResponse getSchedule(@PathVariable Long scheduleId) {
        return ScheduleDetailResponse.from(gymService.findSchedule(scheduleId));
    }
}
