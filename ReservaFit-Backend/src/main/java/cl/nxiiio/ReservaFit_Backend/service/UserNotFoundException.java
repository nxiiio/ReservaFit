package cl.nxiiio.ReservaFit_Backend.service;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User is not registered; call POST /api/users/me first");
    }
}
