package cl.nxiiio.reservafit.usuarios.exception;

public class RutAlreadyRegisteredException extends RuntimeException {

    public RutAlreadyRegisteredException() {
        super("RUT is already registered to another user");
    }
}
