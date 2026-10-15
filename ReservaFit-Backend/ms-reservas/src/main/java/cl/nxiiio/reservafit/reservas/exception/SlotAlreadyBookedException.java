package cl.nxiiio.reservafit.reservas.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException() {
        super("The slot already has an active booking for that date");
    }
}
