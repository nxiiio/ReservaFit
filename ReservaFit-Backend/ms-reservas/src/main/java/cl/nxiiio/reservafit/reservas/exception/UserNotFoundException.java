package cl.nxiiio.reservafit.reservas.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User is not registered; call POST /api/usuarios/me first");
    }
}
