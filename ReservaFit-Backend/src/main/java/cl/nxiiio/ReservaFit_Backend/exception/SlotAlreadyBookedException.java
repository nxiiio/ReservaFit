package cl.nxiiio.ReservaFit_Backend.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException() {
        super("The slot already has an active booking for that date");
    }
}
