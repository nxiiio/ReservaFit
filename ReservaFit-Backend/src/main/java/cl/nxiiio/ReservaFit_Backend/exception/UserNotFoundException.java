package cl.nxiiio.ReservaFit_Backend.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User is not registered; call POST /api/usuarios/me first");
    }
}
