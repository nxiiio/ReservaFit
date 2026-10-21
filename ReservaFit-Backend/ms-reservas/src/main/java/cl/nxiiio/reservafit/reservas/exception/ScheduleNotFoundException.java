package cl.nxiiio.reservafit.reservas.exception;

public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(Long id) {
        super("Schedule " + id + " does not exist");
    }
}
