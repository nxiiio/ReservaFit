package cl.nxiiio.ReservaFit_Backend.exception;

public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(Long id) {
        super("Schedule " + id + " does not exist");
    }
}
