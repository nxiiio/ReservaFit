package cl.nxiiio.ReservaFit_Backend.exception;

public class AlreadyCancelledException extends RuntimeException {

    public AlreadyCancelledException() {
        super("The booking is already canceled");
    }
}
