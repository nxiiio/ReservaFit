package cl.nxiiio.reservafit.reservas.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import cl.nxiiio.reservafit.reservas.client.GymClient;
import cl.nxiiio.reservafit.reservas.client.ScheduleInfo;

// Answers like ms-gimnasios would, without HTTP. Mirrors gym 1 ("Fuerza Barrio Gym")
// of ms-gimnasios' data.sql. An unknown id gives an empty Optional, like a 404 from ms-gimnasios.
class FakeGymClient implements GymClient {

    static final Long GYM_ID = 1L;

    private static final List<ScheduleInfo> GYM_SCHEDULES = List.of(
            schedule(1L, 7),
            schedule(2L, 8),
            schedule(3L, 18),
            schedule(4L, 19),
            schedule(5L, 20));

    private static ScheduleInfo schedule(Long id, int startHour) {
        return new ScheduleInfo(
                id,
                GYM_ID,
                "Fuerza Barrio Gym",
                "Ñuñoa",
                null,
                LocalTime.of(startHour, 0),
                LocalTime.of(startHour + 1, 0));
    }

    @Override
    public Optional<List<ScheduleInfo>> findSchedulesOfGym(Long gymId) {
        if (!GYM_ID.equals(gymId)) {
            return Optional.empty();
        }
        return Optional.of(GYM_SCHEDULES);
    }

    @Override
    public Optional<ScheduleInfo> findSchedule(Long scheduleId) {
        return GYM_SCHEDULES.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst();
    }
}
