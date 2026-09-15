package cl.nxiiio.ReservaFit_Backend.exception;

public class RutAlreadyRegisteredException extends RuntimeException {

    public RutAlreadyRegisteredException() {
        super("RUT is already registered to another user");
    }
}
