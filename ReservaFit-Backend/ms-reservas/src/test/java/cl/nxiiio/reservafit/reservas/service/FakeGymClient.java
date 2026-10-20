package cl.nxiiio.reservafit.reservas.service;

import java.time.LocalTime;
import java.util.List;

import cl.nxiiio.reservafit.reservas.client.GymClient;
import cl.nxiiio.reservafit.reservas.client.ScheduleInfo;
import cl.nxiiio.reservafit.reservas.exception.GymNotFoundException;
import cl.nxiiio.reservafit.reservas.exception.ScheduleNotFoundException;

// Answers like ms-gimnasios would, without HTTP. Mirrors gym 1 ("Fuerza Barrio Gym")
// of ms-gimnasios' data.sql.
class FakeGymClient extends GymClient {

    static final Long GYM_ID = 1L;

    private static final List<ScheduleInfo> GYM_SCHEDULES = List.of(
            schedule(1L, 7),
            schedule(2L, 8),
            schedule(3L, 18),
            schedule(4L, 19),
            schedule(5L, 20));

    FakeGymClient() {
        // Never used: both methods are replaced below
        super("http://localhost:0");
    }

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
    public List<ScheduleInfo> findSchedulesOfGym(Long gymId) {
        if (!GYM_ID.equals(gymId)) {
            throw new GymNotFoundException(gymId);
        }
        return GYM_SCHEDULES;
    }

    @Override
    public ScheduleInfo findSchedule(Long scheduleId) {
        return GYM_SCHEDULES.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }
}
