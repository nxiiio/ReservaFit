package cl.nxiiio.reservafit.reservas.client;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Asks ms-gimnasios for its schedules over HTTP. OpenFeign writes the implementation from
// these annotations (the same ones as in a controller). It calls the service directly
// (app.gimnasios-url), not through the gateway. Both endpoints are public, so no token is sent.
//
// dismiss404: a 404 (unknown gym or schedule) comes back as an empty Optional instead of an
// exception. Any other failure, such as ms-gimnasios being down, throws a FeignException,
// which ApiExceptionHandler turns into a 503.
@FeignClient(name = "ms-gimnasios", url = "${app.gimnasios-url}", dismiss404 = true)
public interface GymClient {

    // The schedules of one gym, ordered by start time
    @GetMapping("/api/gimnasios/{id}/horarios")
    Optional<List<ScheduleInfo>> findSchedulesOfGym(@PathVariable("id") Long gymId);

    // One schedule, with its gym's data
    @GetMapping("/api/gimnasios/horarios/{id}")
    Optional<ScheduleInfo> findSchedule(@PathVariable("id") Long scheduleId);
}
